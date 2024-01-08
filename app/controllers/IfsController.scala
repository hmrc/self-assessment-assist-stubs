/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import controllers.actions.HeaderValidatorAction
import models.{
  Error,
  IFRequest,
  IFRequestPayloadActionLinks,
  IfsInternalServerError500,
  IfsServiceBadRequest400,
  IfsServiceNotAvailable503,
  IfsServiceRequestTimeout408
}
import play.api.Logging
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class IfsController @Inject() (headerValidator: HeaderValidatorAction, cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  val invalidPayload: Error       = Error("INVALID_PAYLOAD", "Submission has not passed validation. Invalid payload.")
  val invalidCorrelationId: Error = Error("INVALID_CORRELATIONID", "Submission has not passed validation. Invalid header CorrelationId.")
  val serviceUnavailable: Error   = Error("SERVICE_UNAVAILABLE", "Dependent systems are currently not responding.")

  def submit(): Action[JsValue] = {
    headerValidator.async(parse.json) { implicit request: Request[JsValue] =>
      Future {
        request.body.validate[IFRequest] match {
          case JsSuccess(value, _) if (value.eventName == "AcknowledgeReport") =>
            logger.info(s"Processing AcknowledgeReport report")
            value.feedbackId match {
              case IfsInternalServerError500.feedbackId   => InternalServerError
              case IfsServiceRequestTimeout408.feedbackId => RequestTimeout
              case IfsServiceNotAvailable503.feedbackId   => ServiceUnavailable(Json.toJson(serviceUnavailable))
              case IfsServiceBadRequest400.feedbackId     => BadRequest(Json.toJson(invalidPayload))
              case _                                      => NoContent
            }
          case JsSuccess(value, _) =>
            if (!hasInvalidLinks(value)) {
              logger.info(s"Processing generate report")
              value.metaData.find(_.contains("calculationId")) match {
                case Some(value) =>
                  value.get("calculationId") match {
                    case Some(IfsInternalServerError500.calculationId)   => InternalServerError
                    case Some(IfsServiceRequestTimeout408.calculationId) => RequestTimeout
                    case Some(IfsServiceBadRequest400.calculationId)     => BadRequest(Json.toJson(invalidCorrelationId))
                    case Some(IfsServiceNotAvailable503.calculationId)   => ServiceUnavailable(Json.toJson(serviceUnavailable))
                    case _                                               => NoContent
                  }
                case _ =>
                  logger.error(s"[IfsController]: calculationId not found bad request")
                  BadRequest(Json.toJson(invalidPayload))
              }
            } else {
              logger.error(s"[IfsController]: Invalid links array ")
              BadRequest(Json.toJson(invalidPayload))
            }
          case _ =>
            logger.error(s"[IfsController]: Failed to validate IFS request")
            BadRequest(Json.toJson(invalidPayload))
        }
      }
    }
  }

  private def hasInvalidLinks(value: IFRequest): Boolean = {
    value.payload.exists { messages =>
      messages.messages.exists { ifRequestPayloadSeq =>
        ifRequestPayloadSeq.exists { ifRequestPayload =>
          hasInvalidSquareBrackets(ifRequestPayload.englishAction.links) ||
          hasInvalidSquareBrackets(ifRequestPayload.welshAction.links)
        }
      }
    }
  }

  private def hasInvalidSquareBrackets(links: Option[Seq[IFRequestPayloadActionLinks]]): Boolean = {
    links.exists {
      _.exists { link =>
        link.linkTitle.contains('[') || link.linkTitle.contains(']') ||
        link.linkUrl.contains('[') || link.linkUrl.contains(']')
      }
    }
  }

}

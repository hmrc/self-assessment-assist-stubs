/*
 * Copyright 2025 HM Revenue & Customs
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

import config.AppConfig
import controllers.actions.HeaderValidatorAction
import models._
import play.api.Logging
import play.api.libs.json.{JsSuccess, JsValue, Json}
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IfsController @Inject() (headerValidator: HeaderValidatorAction, cc: ControllerComponents, appConfig: AppConfig)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  val invalidPayloadError: JsValue = Json.parse(
    """
      |{
      |  "failures": [
      |    {
      |      "code": "INVALID_PAYLOAD",
      |      "reason": "Submission has not passed validation. Invalid payload."
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val invalidCorrelationIdError: JsValue = Json.parse(
    """
      |{
      |  "failures": [
      |    {
      |      "code": "INVALID_CORRELATIONID",
      |      "reason": "Submission has not passed validation. Invalid header CorrelationId."
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val internalServerError: JsValue = Json.parse(
    """
      |{
      |  "failures": [
      |    {
      |      "code": "SERVER_ERROR",
      |      "reason": "IF is currently experiencing problems that require live service intervention."
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val serviceUnavailableError: JsValue = Json.parse(
    """
      |{
      |  "failures": [
      |    {
      |      "code": "SERVICE_UNAVAILABLE",
      |      "reason": "Dependent systems are currently not responding."
      |    }
      |  ]
      |}
    """.stripMargin
  )

  private val isSandboxMode: Boolean = appConfig.disableErrorResponses

  def submit(): Action[JsValue] = {
    headerValidator.async(parse.json) { implicit request: Request[JsValue] =>
      Future {
        request.body.validate[IFRequest] match {
          case JsSuccess(value, _) if value.eventName == "AcknowledgeReport" =>
            logger.info("[IfsController][submit] Processing acknowledge report")
            value.feedbackId match {
              case IfsInternalServerError500.feedbackId   => InternalServerError(internalServerError)
              case IfsServiceRequestTimeout408.feedbackId => RequestTimeout
              case IfsServiceNotAvailable503.feedbackId   => ServiceUnavailable(serviceUnavailableError)
              case IfsServiceBadRequest400.feedbackId     => BadRequest(invalidPayloadError)
              case _                                      => NoContent
            }
          case JsSuccess(value, _) =>
            if (hasInvalidLinks(value)) {
              logger.error("[IfsController][submit] Invalid links array")
              BadRequest(invalidPayloadError)
            } else {
              logger.info("[IfsController][submit] Processing generate report")
              value.metaData.find(_.contains("calculationId")).flatMap(_.get("calculationId")) match {
                case Some(calcId) if isSandboxMode =>
                  logger.info(s"[IfsController][submit] Sandbox mode enabled - returning success response for calculationId: $calcId")
                  NoContent
                case Some(IfsInternalServerError500.calculationId)   => InternalServerError(internalServerError)
                case Some(IfsServiceRequestTimeout408.calculationId) => RequestTimeout
                case Some(IfsServiceBadRequest400.calculationId)     => BadRequest(invalidCorrelationIdError)
                case Some(IfsServiceNotAvailable503.calculationId)   => ServiceUnavailable(serviceUnavailableError)
                case Some(_)                                         => NoContent
                case None                                            =>
                  logger.error("[IfsController][submit] Calculation ID not found bad request")
                  BadRequest(invalidPayloadError)
              }
            }
          case _ =>
            logger.error("[IfsController][submit] Failed to validate IFS request")
            BadRequest(invalidPayloadError)
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

/*
 * Copyright 2023 HM Revenue & Customs
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
import models.{Error, IFRequest}
import play.api.Logging
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class IfsController @Inject()(headerValidator: HeaderValidatorAction,
                              cc: ControllerComponents
                             )(implicit ec: ExecutionContext)
  extends BackendController(cc) with Logging {


  val invalidPayload: Error = Error("INVALID_PAYLOAD", "Submission has not passed validation. Invalid payload.")
  val invalidCorrelationId: Error = Error("INVALID_CORRELATIONID", "Submission has not passed validation. Invalid header CorrelationId.")
  val serviceUnavailable: Error = Error("SERVICE_UNAVAILABLE", "Dependent systems are currently not responding.")

  def submit(): Action[JsValue] = {
    headerValidator.async(parse.json) { implicit request: Request[JsValue] =>
      Future {
        request.body.validate[IFRequest] match {
          case JsSuccess(value, _) => value.metadata.find(_.contains("calculationId")) match {
            case Some(value) => value.get("calculationId") match {
              case Some("404404b4-06e3-4fef-a555-6fd0877dc7ca") => BadRequest(Json.toJson(invalidCorrelationId))
              case Some("408408b4-06e3-4fef-a555-6fd0877dc7ca") => ServiceUnavailable(Json.toJson(serviceUnavailable))
              case _ => NoContent
            }
            case _ => BadRequest(Json.toJson(invalidPayload))
          }
          case _ => BadRequest(Json.toJson(invalidPayload))
        }
      }
    }
  }
}


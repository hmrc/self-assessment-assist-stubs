/*
 * Copyright 2022 HM Revenue & Customs
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
import models.NRSSubmission
import play.api.Logging
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.HashUtil

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class NrsController @Inject()(headerValidator: HeaderValidatorAction,
                              hashUtil: HashUtil,
                              cc: ControllerComponents
                             )(implicit ec: ExecutionContext)
  extends BackendController(cc) with Logging {

  private final val ChecksumFailed = new Status(419)

  private def requestSuccessfulFake: JsValue = {
    val uuid = UUID.randomUUID()
    Json.obj("nrSubmissionId" -> s"$uuid")
  }

  def submit(): Action[JsValue] = {
    headerValidator.async(parse.json) { implicit request: Request[JsValue] =>
      Future {
        request.body.validate[NRSSubmission] match {
          case JsSuccess(value, _) =>
            if (validChecksum(value)) {
              logger.debug(s"[StubNonRepudiationServiceController] Payload received: ${request.body}")
              getReportId(value) match {
                case "a365c0b4-06e3-4fef-a555-16fd08770400" => BadRequest
                case "a365c0b4-06e3-4fef-a555-16fd08770500" => InternalServerError(JsString("Internal NRS Submission API error"))
                case "a365c0b4-06e3-4fef-a555-16fd08770502" => BadGateway
                case "a365c0b4-06e3-4fef-a555-16fd08770503" => ServiceUnavailable
                case "a365c0b4-06e3-4fef-a555-16fd08770504" => GatewayTimeout
                case "a365c0b4-06e3-4fef-a555-16fd08770202" => Accepted(requestSuccessfulFake)
                case _ => Accepted(requestSuccessfulFake)
              }
            } else {
              ChecksumFailed
            }
          case JsError(errors) => BadRequest(errors.toString())
        }
      }
    }
  }

  private def validChecksum(submission: NRSSubmission): Boolean = {
    val payload = Json.stringify(hashUtil.decode(submission.payload))
    val hash = hashUtil.getSha256Hex(payload)

    submission.metadata.payloadSha256Checksum == hash
  }

  private def getReportId(submission: NRSSubmission): String = {
    (hashUtil.decode(submission.payload) \ "reportId").as[String]
  }
}

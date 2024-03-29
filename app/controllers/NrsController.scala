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

import controllers.actions.NrsHeaderValidatorAction
import models.{NRSSubmission, NrsAccepted, NrsBadGateway, NrsBadRequest, NrsChecksumFailed, NrsGatewayTimeout, NrsInternalServerError, NrsNetworkTimeout, NrsNotFound, NrsServiceUnavailable, NrsUnauthorised}
import play.api.Logging
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.HashUtil

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class NrsController @Inject()(headerValidator: NrsHeaderValidatorAction,
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
              val feedbackId = getReportId(value)
              feedbackId match {
                case NrsBadRequest.feedbackId           => BadRequest
                case NrsInternalServerError.feedbackId  => InternalServerError(JsString("Internal NRS Submission API error"))
                case NrsBadGateway.feedbackId           => BadGateway
                case NrsServiceUnavailable.feedbackId   => ServiceUnavailable
                case NrsGatewayTimeout.feedbackId       => GatewayTimeout
                case NrsAccepted.feedbackId             => Accepted(requestSuccessfulFake)
                case NrsNetworkTimeout.feedbackId       => RequestTimeout
                case NrsNotFound.feedbackId             => NotFound
                case NrsChecksumFailed.feedbackId       => ChecksumFailed
                case NrsUnauthorised.feedbackId         => Unauthorized
                case _                                  => Accepted(requestSuccessfulFake)
              }
            } else {
              logger.error(s"Nrs checksum failed")
              ChecksumFailed
            }
          case JsError(errors) => logger.error(s"Nrs validation failed")
            BadRequest(errors.toString())
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

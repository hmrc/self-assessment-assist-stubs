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

//package controllers
//
//import play.api.libs.json.{JsValue, Json}
//import play.api.mvc.{Action, ControllerComponents}
//import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
//
//import java.util.UUID
//import javax.inject.{Inject, Singleton}
//import scala.concurrent.Future
//
//@Singleton()
//class NrsController @Inject()(cc: ControllerComponents)
//  extends BackendController(cc) with Logging{
//
//  def submit(): Action[JsValue] = Action.async(parse.json) {
//    request => {
//      def requestSuccesfulFake = {
//        val uuid = new UUID(0, 1)
//        val retSubmissionSuccesful =
//          s"""{
//             |"reportSubmissionId": "${uuid}"
//             |}""".stripMargin
//        val jsonParse = (Json.parse(retSubmissionSuccesful))
//        jsonParse
//      }
//    logger.info(s"NRS request received")
//      Future.successful(Ok(requestSuccesfulFake))
//    }
//  }
//
//}
//


/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package controllers

import controllers.actions.HeaderValidatorAction
import models.NRSSubmission
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.DigestUtils
import play.api.Logging
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton()
class NrsController @Inject()(headerValidator: HeaderValidatorAction,
                              cc: ControllerComponents
                             )
  extends BackendController(cc) with Logging {

  private final val ChecksumFailed = new Status(419)

  def requestSuccesfulFake = {
    val uuid = new UUID(0, 1)
    val retSubmissionSuccesful =
      s"""{
         |"nrSubmissionId": "${uuid}"
         |}""".stripMargin
    val jsonParse = (Json.parse(retSubmissionSuccesful))
    jsonParse
  }


  def submit(): Action[JsValue] = {
    headerValidator.async(parse.json) { implicit request: Request[JsValue] =>
      Future {
        request.body.validate[NRSSubmission] match {
          case JsSuccess(value, _) =>
            if (validateChecksum(value)) {
              logger.debug(s"[StubNonRepudiationServiceController] Payload received: ${request.body}")
              getReportId(value) match {
                case "a365c0b4-06e3-4fef-a555-16fd08770400" => BadRequest
                case "a365c0b4-06e3-4fef-a555-16fd08770500" => InternalServerError(JsString("Internal NRS Submission API error"))
                case "a365c0b4-06e3-4fef-a555-16fd08770502" => BadGateway
                case "a365c0b4-06e3-4fef-a555-16fd08770503" => ServiceUnavailable
                case "a365c0b4-06e3-4fef-a555-16fd08770504" => GatewayTimeout
                case "a365c0b4-06e3-4fef-a555-16fd08770202" => Accepted(requestSuccesfulFake)
                case _ => Accepted(requestSuccesfulFake)
              }
            } else {
              ChecksumFailed
            }
          case JsError(errors) => BadRequest(errors.toString())
        }
      }
    }
  }

  private def validateChecksum(submission: NRSSubmission): Boolean = {
    val payload = decodePayload(submission).toString
    val hash = DigestUtils.sha256Hex(payload)

    submission.metadata.payloadSha256Checksum == hash
  }

  private def getReportId(submission: NRSSubmission): String = {
    (decodePayload(submission) \ "reportId").as[String]
  }

  private def decodePayload(submission: NRSSubmission): JsValue = {
    Json.parse(new String(Base64.decodeBase64(submission.payload)))
  }
}

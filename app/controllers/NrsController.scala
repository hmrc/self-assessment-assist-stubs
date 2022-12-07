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

import config.AppConfig
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
class NrsController @Inject()(appConfig:AppConfig, headerValidator: HeaderValidatorAction, cc: ControllerComponents)
  extends BackendController(cc) with Logging {

  final val ChecksumFailed = new Status(419)

  def submit(): Action[JsValue] = {
    appConfig.nrsOldBehaviour match {
      case true => OldBehaviourSubmit()
      case false => AllowTestingOfApiSubmit()
    }
  }

  //TODO eventually remove this behaviour() and only AllowTestingOfApiSubmit
  //TODO:Done tostop breaking tests.
  def OldBehaviourSubmit(): Action[JsValue] = Action.async(parse.json) {
    request => {
      def requestSuccesfulFake = {
        val uuid = new UUID(0, 1)
        val retSubmissionSuccesful =
          s"""{
             |"reportSubmissionId": "${uuid}"
             |}""".stripMargin
        val jsonParse = (Json.parse(retSubmissionSuccesful))
        jsonParse
      }
    logger.info(s"NRS request received")
      Future.successful(Ok(requestSuccesfulFake))
    }
  }

  def AllowTestingOfApiSubmit(): Action[JsValue] = {
    headerValidator.async(parse.json) { implicit request: Request[JsValue] =>
      Future {
        request.body.validate[NRSSubmission] match {
          case JsSuccess(value, _) =>
            if (validateChecksum(value)) {
              logger.info(s"[StubNonRepudiationServiceController] Payload received: ${request.body}")
              getTrustCorrespondenceName(value) match {
                case "NRS Bad Request" => BadRequest
                case "NRS Bad Gateway" => BadGateway
                case "NRS Unavailable" => ServiceUnavailable
                case "NRS Gateway Timeout" => GatewayTimeout
                case "NRS Error" => InternalServerError(JsString("Internal NRS Submission API error"))
                case _ => Accepted(Json.parse(s"""{"nrSubmissionId": "${UUID.randomUUID()}"}"""))
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

  private def getTrustCorrespondenceName(submission: NRSSubmission): String = {
    (decodePayload(submission) \ "correspondence" \ "name").as[String]
  }

  private def decodePayload(submission: NRSSubmission): JsValue = {
    Json.parse(new String(Base64.decodeBase64(submission.payload)))
  }
}

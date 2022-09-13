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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.Logging

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton()
class NrsController @Inject()(cc: ControllerComponents)
  extends BackendController(cc) with Logging{

  def submit(): Action[JsValue] = Action.async(parse.json) {
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

}





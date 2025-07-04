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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class GenericController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends BackendController(cc) {

  def unsupportedRequestHandler(id: String): Action[JsValue] = Action.async(parse.json) {
    _ => {

      def requestSuccesfulFake = {
        val retSubmissionSuccesful =
          s"""{
             |{
             |   "code":"TEST_ONLY_UNSUPPORTED_HTTP_METHOD",
             |   "reason":"Only GET, and POST HTTP methods are supported by this service. This is a test error only."
             |}
             """.stripMargin
        val jsonParse = (Json.parse(retSubmissionSuccesful))
        jsonParse
      }

      Future(requestSuccesfulFake) map {
        response => Conflict(response)
      }
    }
  }

}





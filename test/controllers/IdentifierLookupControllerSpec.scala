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

import base.SpecBase
import controllers.actions.HeaderValidator
import play.api.http.Status.{ACCEPTED, BAD_REQUEST}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.JsonUtils.jsonFromFile

import scala.concurrent.Future
import scala.util.matching.Regex

class IdentifierLookupControllerSpec extends SpecBase with HeaderValidator {

  private val controller: IdentifierLookupController = app.injector.instanceOf[IdentifierLookupController]


  private def onSubmit(nino: String): Future[Result] = {
    val request: FakeRequest[AnyContent] = FakeRequest("GET", s"/mtd-identifier-lookup/nino/$nino")

    controller.lookup(nino).apply(request)
  }

  "IdentifierLookupController lookup()" when {

    "provided with a known NINO" must {

      "return 200 with the matching MTDITID" in {
        val result = onSubmit("NJ070957A")
        val expectedResponse = Json.parse(
          s"""{
             |  "mtdbsa": "XFIT00618912478"
             |}""".stripMargin
        )

        status(result) must be(OK)
        contentAsJson(result) must be(expectedResponse)
      }
    }

    "provided with an unknown NINO" must {
      "return a 500" in {
        val result = onSubmit("NJ070959A")

        status(result) must be(INTERNAL_SERVER_ERROR)
      }
    }
  }
}

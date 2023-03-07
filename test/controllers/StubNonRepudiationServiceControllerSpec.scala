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
import models.{NrsBadGateway, NrsBadRequest, NrsGatewayTimeout, NrsInternalServerError, NrsServiceUnavailable}
import play.api.http.Status.{ACCEPTED, BAD_REQUEST}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.JsonUtils.jsonFromFile

import scala.concurrent.Future
import scala.util.matching.Regex

class StubNonRepudiationServiceControllerSpec extends SpecBase with HeaderValidator {

  private val controller: NrsController = app.injector.instanceOf[NrsController]

  private val v4UuidRegex: Regex = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[4][0-9A-Fa-f]{3}-[89ABab][0-9A-Fa-f]{3}-[0-9A-Fa-f]{12}$".r

  private def onSubmit(value: JsValue, withValidHeaders: Boolean = true): Future[Result] = {
    val request: FakeRequest[JsValue] = if (withValidHeaders) {
      FakeRequest("POST", "/submission").withBody(value).withHeaders(
        CONTENT_TYPE_HEADER -> "application/json",
        API_KEY_HEADER -> "dummy-api-key"
      )
    } else {
      FakeRequest("POST", "/submission").withBody(value)
    }
    controller.submit().apply(request)
  }

  "StubNonRepudiationServiceController onSubmit" when {

    "auditing the report sent to the user" must {

      "return 202 with a UUID when an acknowledgement is received" in {
        val json = jsonFromFile("/a365c0b4-06e3-4fef-a555-16fd08770202-validNrsEventReportOut.json")

        val result = onSubmit(json)

        status(result) must be(ACCEPTED)
        (contentAsJson(result) \ "nrSubmissionId").as[String] must fullyMatch regex v4UuidRegex
      }

    }

    "acknowledging the report" must {

      "return 202 with a UUID when a report is sent" in {
        val json = jsonFromFile("/a365c0b4-06e3-4fef-a555-16fd08770202-validNrsEventAcknowledgeIn.json")

        val result = onSubmit(json)

        status(result) must be(ACCEPTED)
        (contentAsJson(result) \ "nrSubmissionId").as[String] must fullyMatch regex v4UuidRegex
      }

    }

    "check message payload(encode) payloadSha256Checksum(encode) can be read from file" in {
      val json = jsonFromFile("/validNrsEventAcknowledgeChecksumSha.json")

      (json \ "payload").as[String] must be("eyJyZXBvcnRJZCI6ImEzNjVjMGI0LTA2ZTMtNGZlZi1hNTU1LTE2ZmQwODc3ZGM3YyJ9")
      (json \ "metadata" \ "payloadSha256Checksum").as[String] must be("bb895fc5f392e75750784dc4cc3fe9d4055516dfe012c3ae3dc09764dfa19413")
    }

    "return 401 Unauthorised when invalid headers received" in {
      val json = Json.parse(s"""{"test": "value"}""")
      val result = onSubmit(json, withValidHeaders = false)
      status(result) must be(UNAUTHORIZED)
    }
  }

  "StubNonRepudiationServiceController when error occurs onSubmit" must {

    def runTest(harness: TestHarness): Unit = {
      s"${harness.name}" in {
        val json = jsonFromFile(harness.resourcePath)
        val result = onSubmit(json)
        status(result) must be(harness.response)
      }
    }

    case class TestHarness(name: String, resourcePath: String, response: Int)

    val errorTests = Seq(
      TestHarness("return 400 when invalid nrs json received", s"/${NrsBadRequest.feedbackId}-invalidNrsEventAcknowledge.json", BAD_REQUEST),
      TestHarness("return 419 Checksum Failed received when decoded payload does match the sha/checksum", "/a365c0b4-06e3-4fef-a555-16fd08770419-RegistrationWithBadChecksumEvent.json", 419),
      TestHarness("return 500 when there is an internal server error", s"/${NrsInternalServerError.feedbackId}-nrsServiceErrorEvent.json", INTERNAL_SERVER_ERROR),
      TestHarness("return 502 when NRS returns a Bad Gateway error", s"/${NrsBadGateway.feedbackId}-nrsBadGatewayEvent.json", BAD_GATEWAY),
      TestHarness("return 503 when NRS is unavailable", s"/${NrsServiceUnavailable.feedbackId}-nrsServiceUnavailableEvent.json", SERVICE_UNAVAILABLE),
      TestHarness("return 504 when NRS gateway times out", s"/${NrsGatewayTimeout.feedbackId}-nrsGatewayTimeoutEvent.json", GATEWAY_TIMEOUT)
    )

    errorTests.foreach(runTest)

  }
}

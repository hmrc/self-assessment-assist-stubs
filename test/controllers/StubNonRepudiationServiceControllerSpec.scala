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

import base.SpecBase
import controllers.actions.HeaderValidator
import org.apache.commons.codec.digest.DigestUtils
import play.api.http.Status.{ACCEPTED, BAD_REQUEST}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.JsonUtils.{base64JsonFromFile, jsonFromFile}

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.{Base64, UUID}
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.matching.Regex

class StubNonRepudiationServiceControllerSpec extends SpecBase with HeaderValidator {

  val controller: NrsController = app.injector.instanceOf[NrsController]

  val v4UuidRegex: Regex = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[4][0-9A-Fa-f]{3}-[89ABab][0-9A-Fa-f]{3}-[0-9A-Fa-f]{12}$".r
  val uuidRet = (new UUID(0, 1)).toString

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


  @Singleton
  class HashUtil @Inject()() {

    private def sha256: MessageDigest = MessageDigest.getInstance("SHA-256")

    def encode(value: String): String = Base64.getUrlEncoder.encodeToString(value.getBytes(StandardCharsets.UTF_8))

    //TODO ensure that service is using getUrlEncoder.
    def getHash(value: String): String = sha256.digest(value.getBytes()).map("%02x" format _).mkString

  }

  "StubNonRepudiationServiceController onSubmit" should {

    // Use this to quickly generate the payload data and checksum.
//            "Generate output" in {
//              val hashUtil: HashUtil = new HashUtil
//              val payload = "{\"reportId\":\"a365c0b4-06e3-4fef-a555-16fd0877dc7c\"}"
//              val payloadBase64 = hashUtil.encode(payload)
//              val payloadSha = hashUtil.getHash(payload)
//              println (s"payload original::${payload}")
//              println (s"payloadBase64::${payloadBase64}")
//              println (s"payloadSha::${payloadSha}")
//
//              true must be( true )
//            }

    "simple base64 encode hard coded" in {
      val hashUtil: HashUtil = new HashUtil
      val payload = "{\"reportId\":\"a365c0b4-06e3-4fef-a555-16fd0877dc7c\"}"
      val sha = hashUtil.getHash(payload)
      val base64 = hashUtil.encode(payload)

      sha must be("bb895fc5f392e75750784dc4cc3fe9d4055516dfe012c3ae3dc09764dfa19413")
      base64 must be("eyJyZXBvcnRJZCI6ImEzNjVjMGI0LTA2ZTMtNGZlZi1hNTU1LTE2ZmQwODc3ZGM3YyJ9")
    }

    "check message payload(encode) payloadSha256Checksum(encode) can be read from file" in {
      val json = jsonFromFile("/validNrsEventAcknowledgeChecksumShaIn.json")

      (json \ "payload").as[String] must be("eyJyZXBvcnRJZCI6ImEzNjVjMGI0LTA2ZTMtNGZlZi1hNTU1LTE2ZmQwODc3ZGM3YyJ9")
      (json \ "metadata" \ "payloadSha256Checksum").as[String] must be("bb895fc5f392e75750784dc4cc3fe9d4055516dfe012c3ae3dc09764dfa19413")
    }


    "return 202 with a UUID when a successful registration submission is received" in {
      val json = jsonFromFile("/a365c0b4-06e3-4fef-a555-16fd08770202-validNrsEventAcknowledgeIn.json")

      val result = onSubmit(json)

      status(result) must be(ACCEPTED)

      (contentAsJson(result) \ "nrSubmissionId").as[String] must be(uuidRet)
    }
  }

  "return 401 Unauthorised when invalid headers received" in {
    val json = jsonFromFile("/a365c0b4-06e3-4fef-a555-16fd08770401-validNrsRegistrationEvent.json")
    val result = onSubmit(json, withValidHeaders = false)
    status(result) must be(UNAUTHORIZED)
  }


  "StubNonRepudiationServiceController onSubmit errors" should {

    def runTest(description: String, filename: String, submitStateRet: Int): Unit = {
      s"Test::${description}" in {
        val json = jsonFromFile(filename)
        val result = onSubmit(json)
        status(result) must be(submitStateRet)

      }
    }

    val errorInErrorOut = Seq(
      ("return 400 when invalid nrs json received", "/a365c0b4-06e3-4fef-a555-16fd08770400-invalidNrsEventAcknowledge.json", BAD_REQUEST),
      //       ( "return 401 Unauthorised when invalid headers received", "/a365c0b4-06e3-4fef-a555-16fd08770401-validNrsRegistrationEvent.json", UNAUTHORIZED ) ,
      ("return 419 Checksum Failed received when decoded payload does match the sha/checksum", "/a365c0b4-06e3-4fef-a555-16fd08770419-RegistrationWithBadChecksumEvent.json", 419),
      ("return 500 when there is an internal server error", "/a365c0b4-06e3-4fef-a555-16fd08770500-nrsServiceErrorEvent.json", INTERNAL_SERVER_ERROR),
      ("return 502 when NRS returns a Bad Gateway error", "/a365c0b4-06e3-4fef-a555-16fd08770502-nrsBadGatewayEvent.json", BAD_GATEWAY),
      ("return 503 when NRS is unavailable", "/a365c0b4-06e3-4fef-a555-16fd08770503-nrsServiceUnavailableEvent.json", SERVICE_UNAVAILABLE),
      ("return 504 when NRS gateway times out", "/a365c0b4-06e3-4fef-a555-16fd08770504-nrsGatewayTimeoutEvent.json", GATEWAY_TIMEOUT)
    )

    errorInErrorOut.foreach(args => (runTest _).tupled(args))

  }
}

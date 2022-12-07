/*
 * Copyright 2022 HM Revenue & Customs
 *
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

import scala.concurrent.Future
import scala.util.matching.Regex

class StubNonRepudiationServiceControllerSpec extends SpecBase with HeaderValidator {

  val controller: NrsController = app.injector.instanceOf[NrsController]

  val v4UuidRegex: Regex = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[4][0-9A-Fa-f]{3}-[89ABab][0-9A-Fa-f]{3}-[0-9A-Fa-f]{12}$".r

  private def onSubmit(value: JsValue, withValidHeaders: Boolean = true): Future[Result] = {
    val request: FakeRequest[JsValue] = if (withValidHeaders) {
      FakeRequest("POST", "/submission").withBody(value).withHeaders(
        CONTENT_TYPE_HEADER -> "application/json",
        API_KEY_HEADER -> "2880d8aa-4691-49a4-aa6a-99191a51b9ef"
      )
    } else {
      FakeRequest("POST", "/submission").withBody(value)
    }
    controller.submit().apply(request)
  }

  "StubNonRepudiationServiceController onSubmit" should {

    "return 202 with a UUID when a successful registration submission is received" in {
      val json = jsonFromFile("/validRegistrationEvent.json")
      val result = onSubmit(json)
      status(result) must be(ACCEPTED)
      (contentAsJson(result) \ "nrSubmissionId").as[String] must fullyMatch regex v4UuidRegex
    }

    "return 202 with a UUID when a successful variation submission is received" in {
      val json = jsonFromFile("/validVariationEvent.json")
      val result = onSubmit(json)
      status(result) must be(ACCEPTED)
      (contentAsJson(result) \ "nrSubmissionId").as[String] must fullyMatch regex v4UuidRegex
    }

    "return 202 with a UUID when a successful non tax variation submission is received" in {
      val json = jsonFromFile("/validNonTaxVariationEvent.json")
      val result = onSubmit(json)
      status(result) must be(ACCEPTED)
      (contentAsJson(result) \ "nrSubmissionId").as[String] must fullyMatch regex v4UuidRegex
    }

    "return 400 when invalid json received" in {
      val json = jsonFromFile("/invalidEvent.json")
      val result = onSubmit(json)
      status(result) must be(BAD_REQUEST)
    }

    "return 401 Unauthorised when invalid headers received" in {
      val json = jsonFromFile("/validRegistrationEvent.json")
      val result = onSubmit(json, withValidHeaders = false)
      status(result) must be(UNAUTHORIZED)
    }

    "return 419 Page Expired when Checksum Failed received" in {
      val json = jsonFromFile("/RegistrationWithBadChecksumEvent.json")
      val result = onSubmit(json)
      status(result) must be(419)
    }

    "return 500 when there is an internal server error" in {
      val json = jsonFromFile("/serviceErrorEvent.json")
      val result = onSubmit(json)
      status(result) must be(INTERNAL_SERVER_ERROR)
    }

    "return 502 when NRS returns a Bad Gateway error" in {
      val json = jsonFromFile("/badGatewayEvent.json")
      val result = onSubmit(json)
      status(result) must be(BAD_GATEWAY)
    }

    "return 503 when NRS is unavailable" in {
      val json = jsonFromFile("/serviceUnavailableEvent.json")
      val result = onSubmit(json)
      status(result) must be(SERVICE_UNAVAILABLE)
    }

    "return 504 when NRS gateway times out" in {
      val json = jsonFromFile("/gatewayTimeoutEvent.json")
      val result = onSubmit(json)
      status(result) must be(GATEWAY_TIMEOUT)
    }

    "generate base64 encoding for payload" ignore {
      val x = base64JsonFromFile("/gatewayTimeoutPayload.json")
      x must equal("")
    }

    "generate checksum to verify payload" ignore {
      val json = jsonFromFile("/gatewayTimeoutPayload.json")
      val x = DigestUtils.sha256Hex(Json.stringify(json))
      x must equal("")
    }
  }
}

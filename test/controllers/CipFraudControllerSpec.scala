/*
 * Copyright 2025 HM Revenue & Customs
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
import config.AppConfig
import models.FraudRiskRequest
import play.api.Configuration
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CipFraudControllerSpec extends SpecBase {

  private def makeAppConfig(disableErrorResponses: Boolean): AppConfig =
    new AppConfig(Configuration.from(Map("feature-switch.disable-error-responses" -> disableErrorResponses)))

  private class Test(disableErrorResponses: Boolean = false) {
    private val appConfig: AppConfig = makeAppConfig(disableErrorResponses)

    val controller: CipFraudController = new CipFraudController(stubControllerComponents(), appConfig)
  }

  private def fraudRiskRequest(nino: String) = FraudRiskRequest(
    nino = Some(nino),
    fraudRiskHeaders = Map.empty
  )

  private def callSubmitFraudInfo(controller: CipFraudController, requestBody: JsValue): Future[Result] = {
    val request: FakeRequest[JsValue] = FakeRequest("POST", "/fraud").withBody(requestBody)

    controller.submitFraudInfo().apply(request)
  }

  private def testSubmitFraudInfo(controller: CipFraudController,
                                  nino: String,
                                  expectedStatus: Int,
                                  expectedBody: Option[JsValue]): Unit = {
    val requestBody: JsValue = Json.toJson(fraudRiskRequest(nino))

    val result: Future[Result] = callSubmitFraudInfo(controller, requestBody)

    status(result) mustBe expectedStatus
    expectedBody match {
      case Some(expectedJson) => contentAsJson(result) mustBe expectedJson
      case None               => contentAsString(result) mustBe empty
    }
  }

  private def expectedOutcome(controller: CipFraudController,
                              disableErrorResponses: Boolean,
                              errorStatus: Int,
                              errorBody: Option[JsValue]): (Int, Option[JsValue]) = {
    if (disableErrorResponses) {
      (OK, Some(controller.successResponse))
    } else {
      (errorStatus, errorBody)
    }
  }

  private val featureSwitchTestCases: Seq[(Boolean, String)] = Seq((true, "enabled"), (false, "disabled"))

  "CipFraudController" when {
    "submitFraudInfo" when {
      featureSwitchTestCases.foreach { case (disableErrorResponses, scenario) =>
        s"feature switch is $scenario" must {
          Seq("NJ070957A", "ZZ123456A").foreach { nino =>
            s"return 200 OK for valid NINO $nino" in new Test(disableErrorResponses) {
              val expectedResponse: JsValue = controller.successResponse

              testSubmitFraudInfo(controller, nino, OK, Some(expectedResponse))
            }
          }

          "return the expected response for NINO AA088213C" in new Test(disableErrorResponses) {
            val (expectedStatus, expectedResponse): (Int, Option[JsValue]) = expectedOutcome(
              controller, disableErrorResponses, INTERNAL_SERVER_ERROR, None
            )

            testSubmitFraudInfo(controller, "AA088213C", expectedStatus, expectedResponse)
          }

          "return the expected response for NINO ME636062B" in new Test(disableErrorResponses) {
            val (expectedStatus, expectedResponse): (Int, Option[JsValue]) = expectedOutcome(
              controller, disableErrorResponses, REQUEST_TIMEOUT, None
            )

            testSubmitFraudInfo(controller, "ME636062B", expectedStatus, expectedResponse)
          }

          "return the expected response for NINO JL530692C" in new Test(disableErrorResponses) {
            val (expectedStatus, expectedResponse): (Int, Option[JsValue]) = expectedOutcome(
              controller, disableErrorResponses, BAD_REQUEST, Some(controller.failureResponse)
            )

            testSubmitFraudInfo(controller, "JL530692C", expectedStatus, expectedResponse)
          }
        }
      }

      "an invalid request is supplied must return 400 BAD_REQUEST" in new Test() {
        val result: Future[Result] = callSubmitFraudInfo(controller, JsObject.empty)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) must include("JsonValidationError")
      }
    }
  }
}

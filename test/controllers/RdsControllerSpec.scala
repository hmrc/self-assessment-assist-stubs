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
import common.StubResource
import config.AppConfig
import models._
import play.api.Configuration
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.CommonData.{calcIdMappings, feedbackIdAndCorrelationIdMapping}

import scala.concurrent.Future

class RdsControllerSpec extends SpecBase {

  private def makeAppConfig(disableErrorResponses: Boolean): AppConfig =
    new AppConfig(Configuration.from(Map("feature-switch.disable-error-responses" -> disableErrorResponses)))

  private class Test(disableErrorResponses: Boolean = false, forceMissingResource: Boolean = false) {
    private val appConfig: AppConfig = makeAppConfig(disableErrorResponses)

    val stubResource: StubResource = new StubResource(appConfig) {
      override def findResource(path: String): Option[String] = if (forceMissingResource) None else super.findResource(path)
    }

    val controller: RdsController = new RdsController(stubControllerComponents(), stubResource) {
      override def sandboxFeedbackId: String = FeedbackForDefaultResponse.feedbackId
      override def sandboxCorrelationId: String = FeedbackForDefaultResponse.correlationId
    }
  }

  private def generateRequestBody(calculationId: String, taxYear: Int): JsValue = Json.parse(
    s"""
      |{
      |  "inputs": [
      |    {
      |      "name": "calculationId",
      |      "value": "$calculationId"
      |    },
      |    {
      |      "name": "nino",
      |      "value": "NJ070957A"
      |    },
      |    {
      |      "name": "taxYear",
      |      "value": $taxYear
      |    },
      |    {
      |      "name": "customerType",
      |      "value": "T"
      |    },
      |    {
      |      "name": "agentRef",
      |      "value": ""
      |    },
      |    {
      |      "name": "preferredLanguage",
      |      "value": "EN"
      |    },
      |    {
      |      "name": "fraudRiskReportScore",
      |      "value": 0
      |    },
      |    {
      |      "name": "fraudRiskReportHeaders",
      |      "value": [
      |        {
      |          "metadata": [
      |            {
      |              "KEY": "string"
      |            },
      |            {
      |              "VALUE": "string"
      |            }
      |          ]
      |        },
      |        {
      |          "data": [
      |            [
      |              "Gov-Client-MAC-Addresses",
      |              ""
      |            ],
      |            [
      |              "Gov-Client-Timezone",
      |              ""
      |            ]
      |          ]
      |        }
      |      ]
      |    },
      |    {
      |      "name": "fraudRiskReportReasons",
      |      "value": [
      |        {
      |          "metadata": [
      |            {
      |              "Reason": "string"
      |            }
      |          ]
      |        },
      |        {
      |          "data": [
      |            [
      |              "UTR 0128925978251 is 3 hops from a something risky. The average UTR is 4.7 hops from something risky."
      |            ],
      |            [
      |              "DEVICE_ID e171dda8-bd00-415b-962b-b169b8b777a4 has been previously marked as Fraud. The average DEVICE_ID is 5.1 hops from something risky"
      |            ],
      |            [
      |              "NINO AB182561B is 2 hops from something risky. The average NINO is 3.1 hops from something risky."
      |            ]
      |          ]
      |        }
      |      ]
      |    }
      |  ]
      |}
    """.stripMargin
  )

  private def acknowledgeRequestBody(feedbackId: String, correlationId: String): JsValue = Json.parse(
    s"""
      |{
      |  "inputs": [
      |    {
      |      "name": "feedbackId",
      |      "value": "$feedbackId"
      |    },
      |    {
      |      "name": "nino",
      |      "value": "NJ070957A"
      |    },
      |    {
      |      "name": "correlationId",
      |      "value": "$correlationId"
      |    }
      |  ]
      |}
    """.stripMargin
  )

  private def callGenerateReport(controller: RdsController, requestBody: JsValue): Future[Result] = {
    val request: FakeRequest[JsValue] = FakeRequest("POST", "/rds/assessments/self-assessment-assist").withBody(requestBody)

    controller.generateReport().apply(request)
  }

  private def callAcknowledgeReport(controller: RdsController, requestBody: JsValue): Future[Result] = {
    val request: FakeRequest[JsValue] = FakeRequest("POST", "/rds/assessments/self-assessment-assist/acknowledge").withBody(requestBody)

    controller.acknowledgeReport().apply(request)
  }

  private def testGenerateReport(controller: RdsController,
                                 calculationId: String,
                                 expectedStatus: Int,
                                 expectedBody: JsValue,
                                 taxYear: Int = 2021): Unit = {
    val requestBody: JsValue = generateRequestBody(calculationId, taxYear)

    val result: Future[Result] = callGenerateReport(controller, requestBody)

    status(result) mustBe expectedStatus
    contentAsJson(result) mustBe expectedBody
  }

  private def testAcknowledgeReport(controller: RdsController,
                                    feedbackId: String,
                                    correlationId: String,
                                    expectedStatus: Int,
                                    expectedBody: JsValue): Unit = {
    val requestBody: JsValue = acknowledgeRequestBody(feedbackId, correlationId)

    val result: Future[Result] = callAcknowledgeReport(controller, requestBody)

    status(result) mustBe expectedStatus
    contentAsJson(result) mustBe expectedBody
  }

  private val featureSwitchTestCases: Seq[(Boolean, String)] = Seq((true, "enabled"), (false, "disabled"))

  "RdsController" when {
    featureSwitchTestCases.foreach { case (disableErrorResponses, scenario) =>
      s"generateReport and feature switch is $scenario" must {
        "return 201 CREATED with the report when a valid request is supplied" in new Test(disableErrorResponses) {
          val details: CalculationIdDetails = calcIdMappings(FeedbackForDefaultResponse.calculationId)

          val expectedResponse: JsValue = stubResource.loadSubmitResponseTemplate(
            details.calculationId, details.feedbackId, details.correlationId
          )

          testGenerateReport(controller, details.calculationId, CREATED, expectedResponse)
        }

        "return 400 BAD_REQUEST" when {
          "a request with an invalid tax year is supplied" in new Test(disableErrorResponses) {
            val calcId: String = FeedbackForDefaultResponse.calculationId

            val expectedResponse: JsValue = controller.requestValidationFailure("The field taxYear is not a valid value.")

            testGenerateReport(controller, calcId, BAD_REQUEST, expectedResponse, 20111)
          }

          "an invalid request is supplied" in new Test(disableErrorResponses) {
            val expectedResponse: JsValue = controller.invalidBodyError

            val result: Future[Result] = callGenerateReport(controller, JsObject.empty)

            status(result) mustBe BAD_REQUEST
            contentAsJson(result) mustBe expectedResponse
          }
        }

        "return 500 INTERNAL_SERVER_ERROR when template is not found" in new Test(disableErrorResponses, true) {
          val calcId: String = "00000000-0000-0000-0000-000000000000"

          val expectedResponse: JsValue = controller.calcIdNotFoundError

          testGenerateReport(controller, calcId, INTERNAL_SERVER_ERROR, expectedResponse)
        }

        "handle all RDS errors" in new Test(disableErrorResponses) {
          val errorCases: Seq[(Int, String, JsValue)] = Seq(
            (BAD_REQUEST, FeedbackForBadRequest.calculationId, controller.invalidBodyError),
            (NOT_FOUND, RdsNotAvailable404.calculationId, controller.rdsNotAvailableError),
            (REQUEST_TIMEOUT, RdsTimeout408.calculationId, controller.rdsRequestTimeoutError),
            (INTERNAL_SERVER_ERROR, RdsInternalServerError500.calculationId, controller.rdsInternalServerError),
            (SERVICE_UNAVAILABLE, RdsServiceNotAvailable503.calculationId, controller.rdsServiceUnavailableError)
          )

          errorCases.foreach { case (statusCode, calculationId, expectedBody) =>
            val (code, body): (Int, JsValue) = if (disableErrorResponses) {
              val details: CalculationIdDetails = calcIdMappings(FeedbackForDefaultResponse.calculationId)

              val expectedResponse: JsValue = stubResource.loadSubmitResponseTemplate(
                calculationId, details.feedbackId, details.correlationId
              )

              (CREATED, expectedResponse)
            } else {
              (statusCode, expectedBody)
            }

            testGenerateReport(controller, calculationId, code, body)
          }
        }
      }

      s"acknowledgeReport and feature switch is $scenario" must {
        "return 201 CREATED when a valid request is supplied" in new Test(disableErrorResponses) {
          val details: CalculationIdDetails = feedbackIdAndCorrelationIdMapping(FeedbackForDefaultResponse.feedbackId)

          val expectedResponse: JsValue = stubResource.loadAckResponseTemplate(details.feedbackId, "NJ070957A", "response/acknowledge/feedback-ack-202.json")

          testAcknowledgeReport(controller, details.feedbackId, details.correlationId, CREATED, expectedResponse)
        }

        "return 400 BAD_REQUEST when an invalid request is supplied" in new Test(disableErrorResponses) {
          val expectedResponse: JsValue = controller.invalidBodyError

          val result: Future[Result] = callAcknowledgeReport(controller, JsObject.empty)

          status(result) mustBe BAD_REQUEST
          contentAsJson(result) mustBe expectedResponse
        }

        "return 500 INTERNAL_SERVER_ERROR when template is not found" in new Test(disableErrorResponses, true) {
          val (feedbackId, correlationId): (String, String) = ("00000000-0000-0000-0000-000000000000", "7X57CKG72JVNSSS9SALT")

          val expectedResponse: JsValue = controller.calcIdNotFoundError

          testAcknowledgeReport(controller, feedbackId, correlationId, INTERNAL_SERVER_ERROR, expectedResponse)
        }

        "handle all RDS errors" in new Test(disableErrorResponses) {
          val errorCases: Seq[(Int, String, String, JsValue)] = Seq(
            (BAD_REQUEST, FeedbackForBadRequest.feedbackId, FeedbackForBadRequest.correlationId, controller.invalidBodyError),
            (NOT_FOUND, RdsNotAvailable404.feedbackId, RdsNotAvailable404.correlationId, controller.rdsNotAvailableError),
            (REQUEST_TIMEOUT, RdsTimeout408.feedbackId, RdsTimeout408.correlationId, controller.rdsRequestTimeoutError),
            (INTERNAL_SERVER_ERROR, RdsInternalServerError500.feedbackId, RdsInternalServerError500.correlationId, controller.rdsInternalServerError),
            (SERVICE_UNAVAILABLE, RdsServiceNotAvailable503.feedbackId, RdsServiceNotAvailable503.correlationId, controller.rdsServiceUnavailableError)
          )

          errorCases.foreach { case (statusCode, feedbackId, correlationId, expectedBody) =>
            val (code, body): (Int, JsValue) = if (disableErrorResponses) {
              val expectedResponse: JsValue = stubResource.loadAckResponseTemplate(feedbackId, "NJ070957A", "response/acknowledge/feedback-ack-202.json")

              (CREATED, expectedResponse)
            } else {
              (statusCode, expectedBody)
            }

            testAcknowledgeReport(controller, feedbackId, correlationId, code, body)
          }
        }
      }
    }

    "sandboxFeedbackId" must {
      "generate a valid UUID" in new Test() {
        override val controller: RdsController = new RdsController(stubControllerComponents(), stubResource)

        controller.sandboxFeedbackId must fullyMatch regex "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$".r
      }
    }

    "sandboxCorrelationId" must {
      "generate a valid 64-character uppercase alphanumeric ID" in new Test() {
        override val controller: RdsController = new RdsController(stubControllerComponents(), stubResource)

        controller.sandboxCorrelationId must fullyMatch regex "^[A-Z0-9]{64}$".r
      }
    }
  }
}

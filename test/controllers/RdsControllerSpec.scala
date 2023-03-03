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
import common.StubResource
import models.{FeedbackForBadRequest, FeedbackHttp201ResponseCode204, FeedbackHttp201ResponseCode404, FeedbackOneHttp201ResponseCode201, RdsInternalServerError500, RdsNotAvailable404, RdsServiceNotAvailable503, RdsTimeout408}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.CommonData.{calcIdMappings, feedbackIdAndCorrelationIdMapping}

import scala.concurrent.Future

class RdsControllerSpec extends SpecBase with StubResource{

  val generateReportRequestBody = s"""{
                        |  "inputs": [
                        |    {
                        |      "name": "calculationId",
                        |      "value": "testCalculationIdValue"
                        |    },
                        |    {
                        |      "name": "nino",
                        |      "value": "NJ070957A"
                        |    },
                        |    {
                        |      "name": "taxYear",
                        |      "value": "2021-22"
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
                        |}""".stripMargin


  val acknowledgeReportRequestBody = s"""{
                                        |  "inputs": [
                                        |    {
                                        |      "name": "feedbackId",
                                        |      "value": "testFeedbackIdValue"
                                        |    },
                                        |    {
                                        |      "name": "nino",
                                        |      "value": "NJ070957A"
                                        |    },
                                        |    {
                                        |      "name": "correlationId",
                                        |      "value": "testCorrelationIdValue"
                                        |    }
                                        |  ]
                                        |}""".stripMargin

  private val controller: RdsController = app.injector.instanceOf[RdsController]

  private def callGenerateReport(value: JsValue): Future[Result] = {
    val request: FakeRequest[JsValue] =
      FakeRequest("POST", "/rds/assessments/self-assessment-assist").withBody(value)

    controller.generateReport().apply(request)
  }

  private def callAcknowledgeReport(value: JsValue): Future[Result] = {
    val request: FakeRequest[JsValue] =
      FakeRequest("POST", "/rds/assessments/self-assessment-assist/acknowledge").withBody(value)

    controller.acknowledgeReport().apply(request)
  }

  "RdsController generateReport" when {

    "provided with a valid request" must {
      "return a http status as created with the Report" in {
        val calculationIdUnderTest = calcIdMappings(FeedbackOneHttp201ResponseCode201.calculationId)
        val result = callGenerateReport(Json.parse(generateReportRequestBody.replace("testCalculationIdValue",calculationIdUnderTest.calculationId)))

        val expectedResponse =
          loadSubmitResponseTemplate(
            calculationIdUnderTest.calculationId,
            calculationIdUnderTest.feedbackId,
            calculationIdUnderTest.correlationId
          )

        status(result) must be(CREATED)
        contentAsJson(result) must be(expectedResponse)
      }
    }

    "provided with a valid request with calculationId that has no feedback in RDS" must {
      "return a http status as created with the Report" in {
        val calculationIdUnderTest = calcIdMappings(FeedbackHttp201ResponseCode204.calculationId)
        val result = callGenerateReport(Json.parse(generateReportRequestBody.replace("testCalculationIdValue",calculationIdUnderTest.calculationId)))

        val expectedResponse =
          loadSubmitResponseTemplate(
            FeedbackHttp201ResponseCode204.calculationId,
            FeedbackHttp201ResponseCode204.feedbackId,
            FeedbackHttp201ResponseCode204.correlationId
          )

        status(result) must be(CREATED)
        contentAsJson(result) must be(expectedResponse)
      }
    }

    "provided with an valid request, but calcualationId is not available " must {
      "return a http status as created with body response code 404" in {
        val calculationIdUnderTest = calcIdMappings(FeedbackHttp201ResponseCode404.calculationId)
        val result = callGenerateReport(Json.parse(generateReportRequestBody.replace("testCalculationIdValue",calculationIdUnderTest.calculationId)))
        val expectedResponse =
          loadSubmitResponseTemplate(
            FeedbackHttp201ResponseCode404.calculationId,
            FeedbackHttp201ResponseCode404.feedbackId,
            FeedbackHttp201ResponseCode404.correlationId
          )
        status(result) must be(CREATED)
        contentAsJson(result) must be(expectedResponse)
      }
    }

    "provided with an valid request, but RDS is not available " must {
      "return a 404" in {
        val calculationIdUnderTest = calcIdMappings(RdsNotAvailable404.calculationId)
        val result = callGenerateReport(Json.parse(generateReportRequestBody.replace("testCalculationIdValue",calculationIdUnderTest.calculationId)))
        val expectedResponse = Json.parse(s"""
                                             |{
                                             |  "code": "NOT_FOUND",
                                             |  "message": "Scenario to mimic non availabilty of RDS"
                                             |  }
                                             |""".stripMargin)
        status(result) must be(NOT_FOUND)
        contentAsJson(result) must be(expectedResponse)
      }
    }

    "provided with an invalid request" must {
      "return a BAD REQUEST" in {
        val calculationIdUnderTest = calcIdMappings(FeedbackForBadRequest.calculationId)
        val result = callGenerateReport(Json.parse(generateReportRequestBody.replace("testCalculationIdValue",calculationIdUnderTest.calculationId)))
        val expectedResponse = Json.parse(s"""
                                             |{
                                             |  "code": "FORBIDDEN",
                                             |  "message": "Invalid feedback/correlationId"
                                             |  }
                                             |""".stripMargin)
        status(result) must be(BAD_REQUEST)
        contentAsJson(result) must be(expectedResponse)
      }
    }

    "provided with an valid request, but RDS timeout" must {
      "return a REQUEST_TIMEOUT" in {
        val calculationIdUnderTest = calcIdMappings(RdsTimeout408.calculationId)
        val result = callGenerateReport(Json.parse(generateReportRequestBody.replace("testCalculationIdValue",calculationIdUnderTest.calculationId)))
        val expectedResponse = Json.parse(s"""
                                             |{
                                             |  "code": "REQUEST_TIMEOUT",
                                             |  "message": "RDS request timeout"
                                             |  }
                                             |""".stripMargin)
        status(result) must be(REQUEST_TIMEOUT)
        contentAsJson(result) must be(expectedResponse)
      }
    }

    "provided with an valid request, but RDS has internal server error" must {
      "return a INTERNAL_SERVER_ERROR" in {
        val calculationIdUnderTest = calcIdMappings(RdsInternalServerError500.calculationId)
        val result = callGenerateReport(Json.parse(generateReportRequestBody.replace("testCalculationIdValue",calculationIdUnderTest.calculationId)))
        val expectedResponse = Json.parse(s"""
                                             |{
                                             |  "code": "INTERNAL_SERVER_ERROR",
                                             |  "message": "RDS internal server error"
                                             |  }
                                             |""".stripMargin)
        status(result) must be(INTERNAL_SERVER_ERROR)
        contentAsJson(result) must be(expectedResponse)
      }
    }

    "provided with an valid request, but RDS service is not available" must {
      "return a SERVICE_UNAVAIALBLE" in {
        val calculationIdUnderTest = calcIdMappings(RdsServiceNotAvailable503.calculationId)
        val result = callGenerateReport(Json.parse(generateReportRequestBody.replace("testCalculationIdValue",calculationIdUnderTest.calculationId)))
        val expectedResponse = Json.parse(s"""
                                             |{
                                             |  "code": "SERVICE_UNAVAIALBLE",
                                             |  "message": "RDS service not available error"
                                             |  }
                                             |""".stripMargin)
        status(result) must be(SERVICE_UNAVAILABLE)
        contentAsJson(result) must be(expectedResponse)
      }
    }
  }

  "RdsController acknowledgeReport" when {
    "provided with a valid request" must {
      "return a http status as created with the Report" in {
        val calculationIdUnderTest = feedbackIdAndCorrelationIdMapping(FeedbackOneHttp201ResponseCode201.feedbackId)
        val result = callAcknowledgeReport(Json.parse(acknowledgeReportRequestBody
          .replace("testFeedbackIdValue", calculationIdUnderTest.feedbackId)
          .replace("testCorrelationIdValue",calculationIdUnderTest.correlationId)))

        val expectedResponse =
          loadAckResponseTemplate(calculationIdUnderTest.feedbackId, "NJ070957A", "202",s"conf/response/acknowledge/feedback-ack.json")

        status(result) must be(CREATED)
        contentAsJson(result) must be(expectedResponse)
      }
    }

    "provided with an valid request, but RDS is not available " must {
      "return a 404" in {
        val calculationIdUnderTest = feedbackIdAndCorrelationIdMapping(RdsNotAvailable404.feedbackId)
        val result = callAcknowledgeReport(Json.parse(acknowledgeReportRequestBody
          .replace("testFeedbackIdValue", calculationIdUnderTest.feedbackId)
          .replace("testCorrelationIdValue",calculationIdUnderTest.correlationId)))
        val expectedResponse = Json.parse(s"""
                                             |{
                                             |  "code": "NOT_FOUND",
                                             |  "message": "Scenario to mimic non availabilty of RDS"
                                             |  }
                                             |""".stripMargin)
        status(result) must be(NOT_FOUND)
        contentAsJson(result) must be(expectedResponse)
      }
    }

    "provided with an invalid request" must {
      "return a BAD REQUEST" in {
        val calculationIdUnderTest = feedbackIdAndCorrelationIdMapping(FeedbackForBadRequest.feedbackId)
        val result = callAcknowledgeReport(Json.parse(acknowledgeReportRequestBody
          .replace("testFeedbackIdValue", calculationIdUnderTest.feedbackId)
          .replace("testCorrelationIdValue",calculationIdUnderTest.correlationId)))
        val expectedResponse = Json.parse(s"""
                                             |{
                                             |  "code": "FORBIDDEN",
                                             |  "message": "Invalid feedback/correlationId"
                                             |  }
                                             |""".stripMargin)
        status(result) must be(BAD_REQUEST)
        contentAsJson(result) must be(expectedResponse)
      }
    }

    "provided with an valid request, but RDS timeout" must {
      "return a REQUEST_TIMEOUT" in {
        val calculationIdUnderTest = feedbackIdAndCorrelationIdMapping(RdsTimeout408.feedbackId)
        val result = callAcknowledgeReport(Json.parse(acknowledgeReportRequestBody
          .replace("testFeedbackIdValue", calculationIdUnderTest.feedbackId)
          .replace("testCorrelationIdValue",calculationIdUnderTest.correlationId)))
        val expectedResponse = Json.parse(s"""
                                             |{
                                             |  "code": "REQUEST_TIMEOUT",
                                             |  "message": "RDS request timeout"
                                             |  }
                                             |""".stripMargin)
        status(result) must be(REQUEST_TIMEOUT)
        contentAsJson(result) must be(expectedResponse)
      }
    }
  }
}

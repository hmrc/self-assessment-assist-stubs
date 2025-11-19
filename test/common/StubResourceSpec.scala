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

package common

import base.SpecBase
import config.AppConfig
import models.{FeedbackHttp201ResponseCode204, FeedbackHttp201ResponseCode404, RdsInvalidRespWithMissingCalculationId}
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}

class StubResourceSpec extends SpecBase {

  private def makeAppConfig(disableErrorResponses: Boolean): AppConfig =
    new AppConfig(Configuration.from(Map("feature-switch.disable-error-responses" -> disableErrorResponses)))

  private class Test(disableErrorResponses: Boolean = false, forceMissingResource: Boolean = false) {
    private val appConfig: AppConfig = makeAppConfig(disableErrorResponses)

    val stubResource: StubResource = new StubResource(appConfig) {
      override def findResource(path: String): Option[String] = if (forceMissingResource) None else super.findResource(path)
    }
  }

  private val featureSwitchTestCases: Seq[(Boolean, String)] = Seq((true, "enabled"), (false, "disabled"))

  private def expectedJsonFromFile(stubResource: StubResource, fileName: String, replacements: (String, String)*): JsValue =
    stubResource.findResource(fileName).map { content =>
      replacements.foldLeft(content) { case (c, (from, to)) => c.replace(from, to) }
    }.map(Json.parse).get

  "StubResource" when {
    featureSwitchTestCases.foreach { case (disableErrorResponses, scenario) =>
      s"loadSubmitResponseTemplate and feature switch is $scenario" must {
        "return the default success JSON for success scenarios" in new Test(disableErrorResponses) {
          val successCalcIds: Seq[String] = Seq(
            "000090b4-06e3-4fef-a555-6fd0877dc7de",
            "111190b4-06e3-4fef-a555-6fd0877dc7ca",
            "333390b4-06e3-4fef-a555-6fd0877dc7ca",
            "444490b4-06e3-4fef-a555-6fd0877dc7ca",
            "555590b4-06e3-4fef-a555-6fd0877dc7ca",
            "777790b4-06e3-4fef-a555-6fd0877dc7ca"
          )

          successCalcIds.foreach { calcId =>
            val result: JsValue = stubResource.loadSubmitResponseTemplate(
              calculationId = calcId,
              feedbackId = s"fb-$calcId",
              correlationId = s"corr-$calcId"
            )

            val expectedJson: JsValue = expectedJsonFromFile(
              stubResource,
              "response/submit/default-success-response.json",
              "replaceFeedbackId" -> s"fb-$calcId",
              "replaceCalculationId" -> calcId,
              "replaceCorrelationId" -> s"corr-$calcId"
            )

            result mustBe expectedJson
          }
        }

        "return explicit success JSONs for 204 and 404 feedback response codes" in new Test(disableErrorResponses) {
          val explicitCalcIds: Seq[String] = Seq(
            FeedbackHttp201ResponseCode204.calculationId,
            FeedbackHttp201ResponseCode404.calculationId
          )

          explicitCalcIds.foreach { calcId =>
            val result: JsValue = stubResource.loadSubmitResponseTemplate(
              calculationId = calcId,
              feedbackId = s"fb-$calcId",
              correlationId = s"corr-$calcId"
            )

            val expectedJson: JsValue = expectedJsonFromFile(
              stubResource,
              s"response/submit/$calcId-response.json",
              "replaceFeedbackId" -> s"fb-$calcId",
              "replaceCalculationId" -> calcId,
              "replaceCorrelationId" -> s"corr-$calcId"
            )

            result mustBe expectedJson
          }
        }

        "return the correct JSON for the RDS error scenario" in new Test(disableErrorResponses) {
          val calcId: String = RdsInvalidRespWithMissingCalculationId.calculationId

          val result: JsValue = stubResource.loadSubmitResponseTemplate(
            calculationId = calcId,
            feedbackId = s"fb-$calcId",
            correlationId = s"corr-$calcId"
          )

          val expectedFileName: String = if (disableErrorResponses) {
            "response/submit/default-success-response.json"
          } else {
            s"response/submit/$calcId-response.json"
          }

          val expectedJson: JsValue = expectedJsonFromFile(
            stubResource,
            expectedFileName,
            "replaceFeedbackId" -> s"fb-$calcId",
            "replaceCalculationId" -> calcId,
            "replaceCorrelationId" -> s"corr-$calcId"
          )

          result mustBe expectedJson
        }

        "throw IllegalStateException when template file is not found" in new Test(disableErrorResponses, true) {
          an[IllegalStateException] mustBe thrownBy {
            stubResource.loadSubmitResponseTemplate(
              calculationId = "NonExistentCalcId",
              feedbackId = "fb000",
              correlationId = "corr000"
            )
          }
        }
      }
    }

    "loadAckResponseTemplate" must {
      "return the default acknowledge JSON" in new Test() {
        val fileName: String = "response/acknowledge/feedback-ack-202.json"

        val result: JsValue = stubResource.loadAckResponseTemplate(
          feedbackId = "fbAck001",
          nino = "AA123456A",
          fileName = fileName
        )

        val expectedJson: JsValue = expectedJsonFromFile(
          stubResource,
          fileName,
          "replaceFeedbackId" -> "fbAck001",
          "replaceNino" -> "AA123456A"
        )

        result mustBe expectedJson
      }

      "throw IllegalStateException when template file is not found" in new Test() {
        an[IllegalStateException] mustBe thrownBy {
          stubResource.loadAckResponseTemplate(
            feedbackId = "fbAck001",
            nino = "AA123456A",
            fileName = "NonExistentFile"
          )
        }
      }
    }
  }
}

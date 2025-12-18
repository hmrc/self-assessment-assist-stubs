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

package models

import models.RdsRequest.InputWithBoolean
import play.api.libs.json._
import support.UnitSpec
import uk.gov.hmrc.http.BadRequestException

import java.util.UUID

class RdsRequestSpec extends UnitSpec {

  "RdsRequest" should {

    "return correct values when all required inputs are present" in {
      val uuid = UUID.randomUUID()

      val request = RdsRequest(
        Seq(
          RdsRequest.InputWithString("calculationId", uuid.toString),
          RdsRequest.InputWithString("nino", "AA123456A"),
          RdsRequest.InputWithString("feedbackId", "feedback-1"),
          RdsRequest.InputWithString("correlationId", "correlation-1"),
          RdsRequest.InputWithString("fraudRiskReportReasons", "reason1"),
          RdsRequest.InputWithString("taxYear", "2025")
        )
      )

      request.calculationId shouldBe uuid
      request.ninoValue shouldBe "AA123456A"
      request.feedbackId shouldBe "feedback-1"
      request.correlationId shouldBe "correlation-1"
      request.fraudRiskReportReasons should contain("reason1")
      request.taxYear shouldBe "2025"
      request.isValid shouldBe (true, "")
    }

    "fail validation when taxYear is not four digits" in {
      val request = RdsRequest(
        Seq(
          RdsRequest.InputWithString("calculationId", UUID.randomUUID().toString),
          RdsRequest.InputWithString("nino", "AA123456A"),
          RdsRequest.InputWithString("feedbackId", "feedback-1"),
          RdsRequest.InputWithString("correlationId", "correlation-1"),
          RdsRequest.InputWithString("fraudRiskReportReasons", "reason1"),
          RdsRequest.InputWithString("taxYear", "25")
        )
      )

      request.isValid shouldBe (false, "The field taxYear is not a valid value.")
    }

    "throw BadRequestException when calculationId is missing" in {
      val request = RdsRequest(Seq.empty)

      an[BadRequestException] shouldBe thrownBy {
        request.calculationId
      }
    }

    "throw BadRequestException when nino is missing" in {
      val request = RdsRequest(Seq.empty)

      an[BadRequestException] shouldBe thrownBy {
        request.ninoValue
      }
    }

    "throw BadRequestException when fraudRiskReportReasons is missing" in {
      val request = RdsRequest(Seq.empty)

      an[BadRequestException] shouldBe thrownBy {
        request.fraudRiskReportReasons
      }
    }

  }
  "InputWithBoolean JSON format" should {

    "read name and value when value is present" in {
      val json = Json.parse(
        """
          |{
          |  "name": "someFlag",
          |  "value": true
          |}
          |""".stripMargin
      )

      val result = json.validate[InputWithBoolean].get

      result.name shouldBe "someFlag"
      result.value shouldBe true
    }

    "default value to false when value is missing" in {
      val json = Json.parse(
        """
          |{
          |  "name": "someFlag"
          |}
          |""".stripMargin
      )

      val result = json.validate[InputWithBoolean].get

      result.name shouldBe "someFlag"
      result.value shouldBe false
    }

    "write name and value to JSON" in {
      val model = InputWithBoolean(
        name = "someFlag",
        value = true
      )

      val json = Json.toJson(model)

      (json \ "name").as[String] shouldBe "someFlag"
      (json \ "value").as[Boolean] shouldBe true
    }
  }

}

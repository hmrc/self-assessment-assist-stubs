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

import play.api.libs.json.*
import support.UnitSpec

class FraudRiskRequestSpec extends UnitSpec {

  "FraudRiskRequest JSON format" should {

    "deserialize from JSON correctly" in {
      val json = Json.parse(
        """
          |{
          |  "nino": "AA123456A",
          |  "taxYear": "2025",
          |  "utr": { "value": "1234567890" },
          |  "deviceId": "device-1",
          |  "userId": { "value": "user-1" },
          |  "ipAddress": "127.0.0.1",
          |  "bankAccountSortCode": { "value": "12-34-56" },
          |  "bankAccountNumber": { "value": "12345678" },
          |  "email": "test@example.com",
          |  "submissionId": "sub-123",
          |  "fraudRiskHeaders": {
          |    "header1": "value1",
          |    "header2": "value2"
          |  }
          |}
          |""".stripMargin
      )

      val result = json.validate[FraudRiskRequest].get

      result.nino shouldBe Some("AA123456A")
      result.taxYear shouldBe Some("2025")
      result.utr.get.value shouldBe "1234567890"
      result.userId.get.value shouldBe "user-1"
      result.bankAccountSortCode.get.value shouldBe "12-34-56"
      result.bankAccountNumber.get.value shouldBe "12345678"
      result.fraudRiskHeaders("header1") shouldBe "value1"
    }

    "serialize to JSON correctly" in {
      val model = FraudRiskRequest(
        nino = Some("AA123456B"),
        taxYear = Some("2026"),
        utr = Some(UTR("0987654321")),
        deviceId = Some("device-2"),
        userId = Some(UserId("user-2")),
        ipAddress = Some("192.168.0.1"),
        bankAccountSortCode = Some(BankAccountSortCode("65-43-21")),
        bankAccountNumber = Some(BankAccountNumber("87654321")),
        email = Some("hello@example.com"),
        submissionId = Some("sub-456"),
        fraudRiskHeaders = Map("headerA" -> "valueA")
      )

      val json = Json.toJson(model)

      (json \ "nino").as[String] shouldBe "AA123456B"
      (json \ "utr" \ "value").as[String] shouldBe "0987654321"
      (json \ "userId" \ "value").as[String] shouldBe "user-2"
      (json \ "fraudRiskHeaders" \ "headerA").as[String] shouldBe "valueA"
    }

    "round-trip JSON safely" in {
      val model = FraudRiskRequest(
        nino = Some("AA123456C"),
        taxYear = Some("2027"),
        utr = Some(UTR("111222333")),
        deviceId = Some("device-3"),
        userId = Some(UserId("user-3")),
        ipAddress = Some("10.0.0.1"),
        bankAccountSortCode = Some(BankAccountSortCode("11-22-33")),
        bankAccountNumber = Some(BankAccountNumber("11223344")),
        email = Some("roundtrip@example.com"),
        submissionId = Some("sub-789"),
        fraudRiskHeaders = Map("headerX" -> "valueX")
      )

      Json.toJson(model).validate[FraudRiskRequest] shouldBe JsSuccess(model)
    }
  }

  "UTR, UserId, BankAccountSortCode, BankAccountNumber JSON formats" should {

    "serialize and deserialize correctly" in {
      val utr = UTR("1234567890")
      val userId = UserId("user-1")
      val sortCode = BankAccountSortCode("12-34-56")
      val accountNumber = BankAccountNumber("12345678")

      Json.toJson(utr).validate[UTR].get shouldBe utr
      Json.toJson(userId).validate[UserId].get shouldBe userId
      Json.toJson(sortCode).validate[BankAccountSortCode].get shouldBe sortCode
      Json.toJson(accountNumber).validate[BankAccountNumber].get shouldBe accountNumber
    }
  }
}


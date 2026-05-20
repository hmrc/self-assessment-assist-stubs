/*
 * Copyright 2026 HM Revenue & Customs
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

import java.time.LocalDateTime

class NRSSubmissionSpec extends UnitSpec {

  "SearchKeys JSON format" should {

    "read searchKey from reportId field" in {
      val json = Json.parse(
        """
          |{ "reportId": "report-123" }
          |""".stripMargin
      )

      json.validate[SearchKeys].get shouldBe SearchKeys("report-123")
    }

    "write searchKey as searchKey field" in {
      val model = SearchKeys("report-456")

      val json = Json.toJson(model)

      (json \ "searchKey").as[String] shouldBe "report-456"
    }

    "return error when JSON is invalid" in {
      JsObject.empty.validate[SearchKeys] shouldBe a[JsError]
    }
  }

  "MetaData JSON format" should {

    "deserialize from JSON correctly" in {
      val json = Json.parse(
        """
          |{
          |  "businessId": "business-1",
          |  "notableEvent": "event-1",
          |  "payloadContentType": "application/json",
          |  "payloadSha256Checksum": "checksum",
          |  "userSubmissionTimestamp": "2024-01-01T12:00:00",
          |  "identityData": { "foo": "bar" },
          |  "userAuthToken": "token",
          |  "headerData": { "header": "value" },
          |  "searchKeys": {
          |    "reportId": "report-123"
          |  }
          |}
          |""".stripMargin
      )

      val metadata = json.validate[MetaData].get

      metadata.businessId shouldBe "business-1"
      metadata.searchKeys shouldBe SearchKeys("report-123")
    }

    "serialize to JSON correctly" in {
      val model = MetaData(
        "business-2",
        "event-2",
        "application/json",
        "checksum-2",
        LocalDateTime.parse("2024-02-01T10:00:00"),
        Json.obj("foo" -> "bar"),
        "token-2",
        Json.obj("header" -> "value"),
        SearchKeys("report-456")
      )

      val json = Json.toJson(model)

      (json \ "businessId").as[String] shouldBe "business-2"
      (json \ "notableEvent").as[String] shouldBe "event-2"
      (json \ "payloadContentType").as[String] shouldBe "application/json"
      (json \ "payloadSha256Checksum").as[String] shouldBe "checksum-2"
      (json \ "userAuthToken").as[String] shouldBe "token-2"
      (json \ "searchKeys" \ "searchKey").as[String] shouldBe "report-456"
    }

    "fail deserialization when required fields are missing" in {
      val json = Json.parse(
        """
          |{
          |  "businessId": "business-1"
          |}
          |""".stripMargin
      )

      json.validate[MetaData].isError shouldBe true
    }

    "fail deserialization when timestamp is invalid" in {
      val json = Json.parse(
        """
          |{
          |  "businessId": "business-1",
          |  "notableEvent": "event-1",
          |  "payloadContentType": "application/json",
          |  "payloadSha256Checksum": "checksum",
          |  "userSubmissionTimestamp": "invalid-date",
          |  "identityData": {},
          |  "userAuthToken": "token",
          |  "headerData": {},
          |  "searchKeys": {
          |    "reportId": "report-123"
          |  }
          |}
          |""".stripMargin
      )

      json.validate[MetaData].isError shouldBe true
    }

    "return error when JSON is invalid" in {
      JsObject.empty.validate[MetaData] shouldBe a[JsError]
    }
  }

  "NRSSubmission JSON format" should {

    "deserialize from JSON correctly" in {
      val json = Json.parse(
        """
          |{
          |  "payload": "encoded-payload",
          |  "metadata": {
          |    "businessId": "business-1",
          |    "notableEvent": "event-1",
          |    "payloadContentType": "application/json",
          |    "payloadSha256Checksum": "checksum",
          |    "userSubmissionTimestamp": "2024-01-01T12:00:00",
          |    "identityData": {},
          |    "userAuthToken": "token",
          |    "headerData": {},
          |    "searchKeys": {
          |      "reportId": "report-123"
          |    }
          |  }
          |}
          |""".stripMargin
      )

      val submission = json.validate[NRSSubmission].get

      submission.payload shouldBe "encoded-payload"
      submission.metadata.searchKeys shouldBe SearchKeys("report-123")
    }

    "return error when JSON is invalid" in {
      JsObject.empty.validate[NRSSubmission] shouldBe a[JsError]
    }
  }

}

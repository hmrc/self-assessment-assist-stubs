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

import java.time.OffsetDateTime
import play.api.libs.json._
import support.UnitSpec

class IFRequestSpec extends UnitSpec {

  private val model: IFRequest = IFRequest(
    serviceRegime = "ITSA",
    eventName = "test-event",
    eventTimestamp = OffsetDateTime.parse("2026-05-14T10:15:30Z"),
    feedbackId = "feedback-123",
    metaData = List(
      Map(
        "correlationId" -> "corr-123",
        "submissionId"  -> "sub-123"
      ),
      Map(
        "source" -> "stub"
      )
    ),
    payload = Some(
      Messages(
        messages = Some(
          Seq(
            IFRequestPayload(
              messageId = "messageId",
              englishAction = IFRequestPayloadAction(
                title = "title",
                message = "message",
                action = "action",
                path = "path",
                links = None
              ),
              welshAction = IFRequestPayloadAction(
                title = "welsh title",
                message = "welsh message",
                action = "welsh action",
                path = "welsh path",
                links = None
              )
            )
          )
        )
      )
    )
  )

  private val json: JsValue = Json.parse("""
    {
      "serviceRegime": "ITSA",
      "eventName": "test-event",
      "eventTimestamp": "2026-05-14T10:15:30Z",
      "feedbackId": "feedback-123",
      "metaData": [
        {
          "correlationId": "corr-123",
          "submissionId": "sub-123"
        },
        {
          "source": "stub"
        }
      ],
      "payload": {
        "messages": [
          {
            "messageId": "messageId",
            "englishAction": {
              "title": "title",
              "message": "message",
              "action": "action",
              "path": "path"
            },
            "welshAction": {
              "title": "welsh title",
              "message": "welsh message",
              "action": "welsh action",
              "path": "welsh path"
            }
          }
        ]
      }
    }
  """)

  "IFRequest" when {

    "reading JSON" should {
      "return the expected model" in {
        json.as[IFRequest] shouldBe model
      }
    }

    "writing JSON" should {
      "return the expected JSON" in {
        Json.toJson(model) shouldBe json
      }

      "return error when JSON is invalid" in {
        JsObject.empty.validate[IFRequest] shouldBe a[JsError]
      }
    }
  }

}

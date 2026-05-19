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

import play.api.libs.json._
import support.UnitSpec

class IFRequestPayloadSpec extends UnitSpec {

  val model: IFRequestPayload = IFRequestPayload(
    messageId = "messageId",
    englishAction = IFRequestPayloadAction(
      title = "title",
      message = "message",
      action = "action",
      path = "path",
      links = None
    ),
    welshAction = IFRequestPayloadAction(
      title = "TODO - title",
      message = "TODO - message",
      action = "TODO - action",
      path = "TODO - path",
      links = None
    )
  )

  val ifRequestPayloadJson: JsValue = Json.parse("""
      |{
      |   "messageId": "messageId",
      |   "englishAction": {
      |   "title": "title",
      |   "message": "message",
      |   "action": "action",
      |   "path": "path"
      |   },
      |   "welshAction": {
      |   "title": "TODO - title",
      |   "message": "TODO - message",
      |   "action": "TODO - action",
      |   "path": "TODO - path"
      |   }
      |}
      |""".stripMargin)

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        ifRequestPayloadJson.as[IFRequestPayload] shouldBe model
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(model) shouldBe ifRequestPayloadJson
      }

      "return error when JSON is invalid" in {
        JsObject.empty.validate[IFRequestPayload] shouldBe a[JsError]
      }
    }
  }

}

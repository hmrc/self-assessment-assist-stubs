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

import base.SpecBase
import play.api.libs.json._
import org.scalatest.matchers.should.Matchers._

class IFRequestPayloadActionSpec extends SpecBase {

  "IFRequestPayloadAction" must {

    "serialize to JSON correctly without links" in {

      val model = IFRequestPayloadAction(
        title = "title",
        message = "message",
        action = "action",
        path = "/path",
        links = None
      )

      val json = Json.toJson(model)

      json mustBe Json.obj(
        "title"   -> "title",
        "message" -> "message",
        "action"  -> "action",
        "path"    -> "/path"
      )
    }

    "serialize to JSON correctly with links" in {

      val model = IFRequestPayloadAction(
        title = "title",
        message = "message",
        action = "action",
        path = "/path",
        links = Some(
          Seq(
            IFRequestPayloadActionLinks(
              linkTitle = "help",
              linkUrl = "https://example.com"
            )
          )
        )
      )

      val json = Json.toJson(model)

      (json \ "links").as[JsArray].value.size mustBe 1
      (json \ "links" \ 0 \ "linkTitle").as[String] mustBe "help"
    }

    "deserialize from JSON correctly" in {

      val json = Json.obj(
        "title"   -> "title",
        "message" -> "message",
        "action"  -> "action",
        "path"    -> "/path",
        "links" -> Json.arr(
          Json.obj(
            "linkTitle" -> "help",
            "linkUrl"   -> "https://example.com"
          )
        )
      )

      val result = json.as[IFRequestPayloadAction]

      result mustBe IFRequestPayloadAction(
        title = "title",
        message = "message",
        action = "action",
        path = "/path",
        links = Some(
          Seq(
            IFRequestPayloadActionLinks(
              linkTitle = "help",
              linkUrl = "https://example.com"
            )
          )
        )
      )
    }

    "round-trip serialize and deserialize correctly" in {

      val model = IFRequestPayloadAction(
        title = "title",
        message = "message",
        action = "action",
        path = "/path",
        links = Some(
          Seq(
            IFRequestPayloadActionLinks(
              linkTitle = "help",
              linkUrl = "https://example.com"
            )
          )
        )
      )

      Json.toJson(model).as[IFRequestPayloadAction] mustBe model
    }

    "return error when JSON is invalid" in {
      JsObject.empty.validate[IFRequestPayloadAction] shouldBe a[JsError]
    }
  }

}

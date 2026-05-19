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

class IFRequestPayloadActionLinksSpec extends SpecBase {

  "IFRequestPayloadActionLinks" must {

    "serialize to JSON correctly" in {

      val model = IFRequestPayloadActionLinks(
        linkTitle = "title",
        linkUrl = "https://example.com"
      )

      val json = Json.toJson(model)

      json mustBe Json.obj(
        "linkTitle" -> "title",
        "linkUrl"   -> "https://example.com"
      )
    }

    "deserialize from JSON correctly" in {

      val json = Json.obj(
        "linkTitle" -> "title",
        "linkUrl"   -> "https://example.com"
      )

      val result = json.as[IFRequestPayloadActionLinks]

      result mustBe IFRequestPayloadActionLinks(
        linkTitle = "title",
        linkUrl = "https://example.com"
      )
    }

    "round-trip serialize/deserialize correctly" in {

      val model = IFRequestPayloadActionLinks(
        linkTitle = "title",
        linkUrl = "https://example.com"
      )

      val json = Json.toJson(model)

      json.as[IFRequestPayloadActionLinks] mustBe model
    }

    "return error when JSON is invalid" in {
      JsObject.empty.validate[IFRequestPayloadActionLinks] shouldBe a[JsError]
    }
  }

}

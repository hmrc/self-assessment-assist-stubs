/*
 * Copyright 2022 HM Revenue & Customs
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

package utils

import play.api.libs.json.{JsObject, JsString, JsValue}

object ReturnDataCommonData {
  val jsQQ000001A: JsValue = JsObject(
    Seq(
      "code" -> JsString("FORMAT_NINO"),
      "message" -> JsString("The provided NINO is invalid")
    )
  )


  val valueQQ000001A: LookupValue = LookupValue("QQ000001A", 200, jsQQ000001A)
  val kvQQ000001A = KeyValue("QQ000001A", valueQQ000001A)

  val jsQR000001A: JsValue = JsObject(
    Seq(
      "code" -> JsString("FORMAT_NINO"),
      "message" -> JsString("The provided NINO is invalid")
    )
  )

  val valueQR000000A: LookupValue = LookupValue("QR000000A", 200, jsQR000001A)
  // Keys will not match because picks random key.
  val kvQR000000A = KeyValue("QR000000A", valueQR000000A)
  // Pick Random

  val jsQR000002A: JsValue = JsObject(
    Seq(
      "code" -> JsString("FORMAT_NINO"),
      "message" -> JsString("The provided NINO is invalid")
    )
  )
  val jsQR000003A: JsValue = JsObject(
    Seq(
      "code" -> JsString("FORMAT_NINO"),
      "message" -> JsString("The provided NINO is invalid")
    )
  )
  val jsQR000004A: JsValue = JsObject(
    Seq(
      "code" -> JsString("FORMAT_NINO"),
      "message" -> JsString("The provided NINO is invalid")
    )
  )
  val jsQR000005A: JsValue = JsObject(
    Seq(
      "code" -> JsString("FORMAT_NINO"),
      "message" -> JsString("The provided NINO is invalid")
    )
  )
  val jsQR000006A: JsValue = JsObject(
    Seq(
      "code" -> JsString("FORMAT_NINO"),
      "message" -> JsString("The provided NINO is invalid")
    )
  )

  val valueQR000001A: LookupValue = LookupValue("QR000001A", 401, jsQR000001A)
  val valueQR000002A: LookupValue = LookupValue("QR000002A", 402, jsQR000002A)
  val valueQR000003A: LookupValue = LookupValue("QR000003A", 403, jsQR000003A)
  val valueQR000004A: LookupValue = LookupValue("QR000004A", 501, jsQR000004A)
  val valueQR000005A: LookupValue = LookupValue("QR000005A", 502, jsQR000005A)
  val valueQR000006A: LookupValue = LookupValue("QR000006A", 503, jsQR000006A)

  val kvQR000001A = KeyValue("QR000001A", valueQR000001A)
  val kvQR000002A = KeyValue("QR000002A", valueQR000002A)
  val kvQR000003A = KeyValue("QR000003A", valueQR000003A)
  val kvQR000004A = KeyValue("QR000001A", valueQR000004A)
  val kvQR000005A = KeyValue("QR000002A", valueQR000005A)
  val kvQR000006A = KeyValue("QR000003A", valueQR000006A)

}
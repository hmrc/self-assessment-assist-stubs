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

package utils

import play.api.libs.json.{JsValue, Json}

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import javax.inject.{Inject, Singleton}

@Singleton
class HashUtil @Inject()() {

  def encode(value: String): String =
    Base64.getEncoder.encodeToString(value.getBytes(StandardCharsets.UTF_8))
  def decode(payload: String): JsValue =
  Json.parse(new String(Base64.getDecoder.decode(payload), StandardCharsets.UTF_8))

  def getSha256Hex(value: String): String = {
    val digest = MessageDigest.getInstance("SHA-256")
    val bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8))
    bytes.map("%02x".format(_)).mkString
  }
}


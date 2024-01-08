/*
 * Copyright 2024 HM Revenue & Customs
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

object GenerateBase64andSha256Hash {

  // Use this to quickly generate the payload data and checksum.
  def main( args:Array[String]): Unit = {

    val hashUtil: HashUtil = new HashUtil
    val payload = "{\"reportId\":\"a365c0b4-06e3-4fef-a555-16fd0877dc7c\"}"

    hashUtil.encode(payload)
    hashUtil.getSha256Hex(payload)

    ()
  }

}

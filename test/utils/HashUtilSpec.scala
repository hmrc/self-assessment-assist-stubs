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

import base.SpecBase

class HashUtilSpec extends SpecBase {

  private val hashUtil: HashUtil = app.injector.instanceOf[HashUtil]

  "HashUtil" must {

    "generate sha256 and base64 values for a given payload" in {
      val payload = "{\"reportId\":\"a365c0b4-06e3-4fef-a555-16fd0877dc7c\"}"
      val sha = hashUtil.getHash(payload)
      val base64 = hashUtil.encode(payload)

      sha must be("bb895fc5f392e75750784dc4cc3fe9d4055516dfe012c3ae3dc09764dfa19413")
      base64 must be("eyJyZXBvcnRJZCI6ImEzNjVjMGI0LTA2ZTMtNGZlZi1hNTU1LTE2ZmQwODc3ZGM3YyJ9")
    }

  }

}

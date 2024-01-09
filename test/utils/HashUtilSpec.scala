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

import base.SpecBase
import play.api.libs.json.Json

class HashUtilSpec extends SpecBase {

  private val hashUtil: HashUtil = app.injector.instanceOf[HashUtil]

  "HashUtil" must {

    "generate sha256 and base64 values for an acknowledgement" in {
      val payload = Json.stringify(Json.parse(
        """
          |{"reportId":"a365c0b4-06e3-4fef-a555-16fd0877dc7c"}
          |""".stripMargin))

      val base64 = hashUtil.encode(payload)
      val sha = hashUtil.getSha256Hex(payload)

      base64 must be("eyJyZXBvcnRJZCI6ImEzNjVjMGI0LTA2ZTMtNGZlZi1hNTU1LTE2ZmQwODc3ZGM3YyJ9")
      sha must be("bb895fc5f392e75750784dc4cc3fe9d4055516dfe012c3ae3dc09764dfa19413")
    }

    "generate sha256 and base64 values for a report" in {
      val payload = Json.stringify(Json.parse(
        """
          |{
          |    "reportId": "a365c0b4-06e3-4fef-a555-16fd0877dc7c",
          |    "messages": [
          |        {
          |            "title": "Non-Business Income Source",
          |            "body": "You have declared family loan as a source of your income. There have been changes to the rules around non-business sources you may declare, please check the appropriate guidance to see how this impacts you.",
          |            "action": "Check guidance",
          |            "links": [
          |                {
          |                    "title": "[ITSA Guidance, Income Source Guidance]",
          |                    "url": "[www.itsa.gov.uk, www.itsa/incomesources.gov.uk]"
          |                }
          |            ],
          |            "path": "general/non_business_income_sources/income_source"
          |        },
          |        {
          |            "title": "Turnover",
          |            "body": "Your declared turnover of £80,000 appears to be lower than expected based on your income sources, please confirm all turnover is accounted for before submission.",
          |            "action": "Check turnover",
          |            "links": [
          |                {
          |                    "title": "[Accounting for Income]",
          |                    "url": "[www.itsa/incomecompliance.gov.uk]"
          |                }
          |            ],
          |            "path": "general/total_declared_turnover"
          |        }
          |    ],
          |    "nino": "AA000000B",
          |    "taxyear": "2021-22",
          |    "calculationId": "111190b4-06e3-4fef-a555-6fd0877dc7ca",
          |    "correlationId": "a5fht738957jfjf845jgjf855"
          |}
          |""".stripMargin))

      val base64 = hashUtil.encode(payload)
      val sha = hashUtil.getSha256Hex(payload)

      base64 must be("eyJyZXBvcnRJZCI6ImEzNjVjMGI0LTA2ZTMtNGZlZi1hNTU1LTE2ZmQwODc3ZGM3YyIsIm1lc3NhZ2VzIjpbeyJ0aXRsZSI6Ik5vbi1CdXNpbmVzcyBJbmNvbWUgU291cmNlIiwiYm9keSI6IllvdSBoYXZlIGRlY2xhcmVkIGZhbWlseSBsb2FuIGFzIGEgc291cmNlIG9mIHlvdXIgaW5jb21lLiBUaGVyZSBoYXZlIGJlZW4gY2hhbmdlcyB0byB0aGUgcnVsZXMgYXJvdW5kIG5vbi1idXNpbmVzcyBzb3VyY2VzIHlvdSBtYXkgZGVjbGFyZSwgcGxlYXNlIGNoZWNrIHRoZSBhcHByb3ByaWF0ZSBndWlkYW5jZSB0byBzZWUgaG93IHRoaXMgaW1wYWN0cyB5b3UuIiwiYWN0aW9uIjoiQ2hlY2sgZ3VpZGFuY2UiLCJsaW5rcyI6W3sidGl0bGUiOiJbSVRTQSBHdWlkYW5jZSwgSW5jb21lIFNvdXJjZSBHdWlkYW5jZV0iLCJ1cmwiOiJbd3d3Lml0c2EuZ292LnVrLCB3d3cuaXRzYS9pbmNvbWVzb3VyY2VzLmdvdi51a10ifV0sInBhdGgiOiJnZW5lcmFsL25vbl9idXNpbmVzc19pbmNvbWVfc291cmNlcy9pbmNvbWVfc291cmNlIn0seyJ0aXRsZSI6IlR1cm5vdmVyIiwiYm9keSI6IllvdXIgZGVjbGFyZWQgdHVybm92ZXIgb2YgwqM4MCwwMDAgYXBwZWFycyB0byBiZSBsb3dlciB0aGFuIGV4cGVjdGVkIGJhc2VkIG9uIHlvdXIgaW5jb21lIHNvdXJjZXMsIHBsZWFzZSBjb25maXJtIGFsbCB0dXJub3ZlciBpcyBhY2NvdW50ZWQgZm9yIGJlZm9yZSBzdWJtaXNzaW9uLiIsImFjdGlvbiI6IkNoZWNrIHR1cm5vdmVyIiwibGlua3MiOlt7InRpdGxlIjoiW0FjY291bnRpbmcgZm9yIEluY29tZV0iLCJ1cmwiOiJbd3d3Lml0c2EvaW5jb21lY29tcGxpYW5jZS5nb3YudWtdIn1dLCJwYXRoIjoiZ2VuZXJhbC90b3RhbF9kZWNsYXJlZF90dXJub3ZlciJ9XSwibmlubyI6IkFBMDAwMDAwQiIsInRheHllYXIiOiIyMDIxLTIyIiwiY2FsY3VsYXRpb25JZCI6IjExMTE5MGI0LTA2ZTMtNGZlZi1hNTU1LTZmZDA4NzdkYzdjYSIsImNvcnJlbGF0aW9uSWQiOiJhNWZodDczODk1N2pmamY4NDVqZ2pmODU1In0=")
      sha must be("7878cb1036224b770ee6683ab461ac46ab88ffe45a677cce4cd78c6b7870bbf4")
    }

  }

}

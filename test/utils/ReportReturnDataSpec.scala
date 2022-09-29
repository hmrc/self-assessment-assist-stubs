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

import utils.ReturnDataCommonData._
import utils.UnitSpec.UnitSpec

class ReportReturnDataSpec extends UnitSpec {


  "Testing ReportReturnData subsystem with file" should {
    "If file containing json does not exists " in {
      val rrd = new ReportReturnData("reply/nosuchfile", "no valid key")
      rrd.get("QQ000000A") shouldBe None
    }

    "If file containing jason does exsist but the file is empty" in {
      val rrd = new ReportReturnData("test/resources/reply/blankFile.json", "no valid key")

      rrd.get("QQ000000A") shouldBe None
    }

    "Check we can parse a very simple json statement that has no nino in it" in {
      val rrd = new ReportReturnData("test/resources/reply/simpleCorrect.json", "doesNotMatter")

      rrd.get("QQ000000A") shouldBe None
    }

    "If file containing jason does exsist and has a correct json error stament with a valid nino" in {
      val rrd = new ReportReturnData("test/resources/reply/simpleJsonErrorToReturn.json", "test")

      //      rrd.get("QQ000001A") shouldBe jsQQ000001A
      rrd.get("QQ000001A") shouldBe Some(valueQQ000001A)
    }

    "load return results for 3 ninos" in {
      val rrd = new ReportReturnData("test/resources/reply/simple3JsonErrorToReturn.json", "test")

      //      rrd.get("QQ000001A") shouldBe jsQQ000001A
      rrd.get("QR000001A") shouldBe Some(valueQR000001A)
      rrd.get("QR000002A") shouldBe Some(valueQR000002A)
      rrd.get("QR000003A") shouldBe Some(valueQR000003A)
    }

    "load return results for 3 out of 6 ninos" in {
      val rrd = new ReportReturnData("test/resources/reply/simple3JsonErrorToReturn.json", "test")

      //      rrd.get("QQ000001A") shouldBe jsQQ000001A
      rrd.get("QR000001A") shouldBe Some(valueQR000001A)
      rrd.get("QR000002A") shouldBe Some(valueQR000002A)
      rrd.get("QR000003A") shouldBe Some(valueQR000003A)

      rrd.get("QR000004A") shouldBe None
      rrd.get("QR000005A") shouldBe None
      rrd.get("QR000006A") shouldBe None

    }

    "load random return results for 3 ninos" in {
      val rrd = new ReportReturnData("test/resources/reply/simple3JsonErrorToReturn.json", "test")

      val rndError = rrd.get("QR000000A")
      rndError should not be None
      // At least one should be found.
      // TODO: Create test later

    }


    "load a big file used in production. Test to check loads ok" in {
      val rrd = new ReportReturnData("resources/reply/reply.json", "generateReport")

      val rndError = rrd.get("QQ000002A")
      rndError should not be None

    }

  }

}

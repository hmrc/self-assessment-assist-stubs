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

import scala.util.{Random, Try}

case class ReportReturnData(fileName:String, fileterKey:String="generateReport")  {
  val random = new Random()
  val jsonStore = new JsonStore(fileName, fileterKey)

  def get(nino:String) : Option[LookupValue] = {
    nino match {
      case "QR000000A" => {
        val pickFrom = Array(Some("QR000001A"), Some("QR000002A"), Some("QR000003A"))
        val r = random.nextInt(pickFrom.length)
        val sk:Option[String] = Try(pickFrom(r)).toOption.flatten

        val k:Option[LookupValue] = sk.flatMap( x => jsonStore.get(x))
        k

      }
      case _ =>
        val s = jsonStore.get(nino)
        s
    }
  }
}

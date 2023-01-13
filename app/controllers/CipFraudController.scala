/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers

import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}


@Singleton()
class CipFraudController @Inject()(cc: ControllerComponents)
  extends BackendController(cc) with Logging {

  val retSubmissionSuccesful =
    s"""{
       |  "riskScore": 0,
       |  "riskCorrelationId": "123e4567-e89b-12d3-a456-426614174000",
       |  "reasons": [
       |    "UTR 0128925978251 is 3 hops from a something risky. The average UTR is 4.7 hops from something risky.",
       |    "DEVICE_ID e171dda8-bd00-415b-962b-b169b8b777a4 has been previously marked as Fraud. The average DEVICE_ID is 5.1 hops from something risky",
       |    "NINO AB182561B is 2 hops from something risky. The average NINO is 3.1 hops from something risky."
       |  ]
       |}""".stripMargin

  def submitFraudInfo(): Action[JsValue] = Action(parse.json) {
    request => {
      logger.info(s"======Invoked CipFraudController======")
      Ok(Json.parse(retSubmissionSuccesful))
    }
  }
}





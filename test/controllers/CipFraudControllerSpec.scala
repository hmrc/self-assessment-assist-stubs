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

import base.SpecBase
import models.FraudRiskRequest
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.CommonData.ninoMtdIdPairs

import scala.concurrent.Future

class CipFraudControllerSpec extends SpecBase{

  private val controller: CipFraudController = app.injector.instanceOf[CipFraudController]


  private def submitFraudInfo(nino: String,taxYear:String,fraudRiskHeaders:Map[String,String] = Map.empty): Future[Result] = {
    val fraudRiskRequest =new FraudRiskRequest(
      nino= Some(nino),
      taxYear = Some(taxYear),
      fraudRiskHeaders=fraudRiskHeaders
    )
    val fakeRequest: FakeRequest[JsValue] = FakeRequest("POST", s"/fraud").withBody(Json.toJson(fraudRiskRequest))
    controller.submitFraudInfo().apply(fakeRequest)
  }

  "CipFraudController submitFraudInfo()" when {

    "provided with a valid fraud risk request" must {
      "return 200 with the FraudRiskReport" in {
        val nino = "NJ070957A"
        val result = submitFraudInfo(nino,ninoMtdIdPairs(nino))
        val expectedResponse = Json.parse(
          s"""{
             |  "riskScore": 0,
             |  "riskCorrelationId": "123e4567-e89b-12d3-a456-426614174000",
             |  "reasons": [
             |    "UTR 0128925978251 is 3 hops from a something risky. The average UTR is 4.7 hops from something risky.",
             |    "DEVICE_ID e171dda8-bd00-415b-962b-b169b8b777a4 has been previously marked as Fraud. The average DEVICE_ID is 5.1 hops from something risky",
             |    "NINO AB182561B is 2 hops from something risky. The average NINO is 3.1 hops from something risky."
             |  ]
             |}""".stripMargin
        )
        status(result) must be(OK)
        contentAsJson(result) must be(expectedResponse)
      }
    }

    "provided with an invalid request" must {
      "return a 400" in {
        val nino = "JL530692C"
        val result = submitFraudInfo(nino,ninoMtdIdPairs(nino))
        status(result) must be(BAD_REQUEST)
      }
    }

    "provided with an valid request, but an ALB failure " must {
      "return a 500" in {
        val nino = "AA088213C"
        val result = submitFraudInfo(nino,ninoMtdIdPairs(nino))
        status(result) must be(INTERNAL_SERVER_ERROR)
      }
    }
  }
}

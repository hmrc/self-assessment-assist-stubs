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

package controllers

import play.api.libs.json.{JsArray, JsDefined, JsLookupResult, JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.{AcknowledgeReturnData, Logging, ReportReturnData}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton()
class RdsController @Inject()(cc: ControllerComponents)
  extends BackendController(cc) with Logging {

  val reportRequestStore = new ReportReturnData("resources/reply/reply.json", "generateReport")
  val acknowledgeStore = new AcknowledgeReturnData("resources/reply/reply.json", "acknowledge")

  val error =
    s"""
       |{
       |  "code": "NINO_NOT_FOUND",
       |  "message": "The provided NINO was not found"
       |  }
       |""".stripMargin

  def generateReport(): Action[JsValue] = Action.async(parse.json) {
    request => {
      logger.info(s"======Invoked RDS for report generation======")

      val ninoOption = getNinoFromRequest(request.body)
      val json = ninoOption match {
        case None => Json.parse(error)
        case Some(nino) =>
          val reportKey = reportRequestStore.get(nino)
          reportKey match {
            case None => Json.parse(error)
            case Some(lookupValue) => lookupValue.jsonReturn
          }
      }
      Future.successful(Ok(json))
    }
  }

  def acknowledgeReport(): Action[JsValue] = Action.async(parse.json) {
    request => {
      logger.info(s"======Invoked RDS for report generation======")

      val reportKey = acknowledgeStore.get("QQ123456A")
      val json = reportKey match {
        case None => Json.parse(error)
        case Some(lookupValue) => lookupValue.jsonReturn
      }
      Future.successful(Ok(json))
    }
  }

  def getNinoFromRequest(input: JsValue): Option[String] = {
    val inputsJsDefined = (input \ "inputs")
    val inputsArray = inputsJsDefined.as[JsArray].value
    val ninosValueArray = inputsArray.filter(x => (x \ "name").as[String] == "nino")
    val ninos = for {
      ninoJson <- ninosValueArray
      ninoJsDefined = (ninoJson \ "value")
      ninoOpt = ninoJsDefined match {
        case JsDefined(v) =>
          Some(v.as[String])
        case _ => None
      }
    } yield ninoOpt

    val ninoRet = ninos.filter(_ != None).flatten.headOption
    ninoRet
  }

}





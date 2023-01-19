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
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}


@Singleton()
class IdentifierLookupController @Inject()(cc: ControllerComponents)
  extends BackendController(cc) with Logging {

  val responseBody = { mtditid: String =>
    s"""{
       |  "mtdbsa": "$mtditid"
       |}""".stripMargin
  }

  val ninoMtdIdPairs = Map(
    "NJ070957A" -> "XFIT00618912478",
    "MS475730B" -> "XCIT00840041559",
    "WS504231C" -> "XBIT00219774624",
    "XT181899C" -> "XDIT00734159815",
    "JL530692C" -> "XQIT00731178134"
  )

  def lookup(nino: String): Action[AnyContent] = Action {
    request => {
      logger.info(s"======Invoked identifier lookup controller ======")
      ninoMtdIdPairs.get(nino).fold(
        InternalServerError("")
      )(mtdid => Ok(Json.parse(responseBody(mtdid))))
    }
  }
}





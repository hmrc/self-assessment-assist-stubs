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

import play.api.Logging
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

/*
 *  I am trying to implement service stubs for the RDS - Risk Decision Service endpoints.
 *  Those are still under active design. There exists a draft published at:
 *
 *  https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=434373176
 *
 *  As of today the level of technical details and rigour is very poor.
 *  Too many important points describing typical HTTP based APIs are still missing.
 *
 */

@Singleton()
class AssessmentController @Inject()(cc: ControllerComponents)
  extends BackendController(cc) with Logging{

  def generate(): Action[AnyContent] = Action.async {
    logger.info(s"======Invoked AssessmentController======")
    Future.successful(Ok(
      s"""
         | {
         |  "links": [],
         |  "version": 2,
         |  "moduleId": "api_input_test",
         |  "stepId": "execute",
         |  "executionState": "completed",
         |  "outputs": [
         |    {
         |      "name": "APICustOut",
         |      "value": [
         |        {
         |          "metadata": [
         |            {
         |              "TITLE": "string"
         |            },
         |            {
         |              "MESSAGE": "string"
         |            },
         |            {
         |              "ACTION": "string"
         |            },
         |            {
         |              "LINKSTITLE": "string"
         |            },
         |            {
         |              "LINKSURL": "string"
         |            },
         |            {
         |              "TYPE_ID": "string"
         |            }
         |          ]
         |        },
         |        {
         |          "data": [
         |            [
         |              "Turnover and Cost of Sales",
         |              "Your cost of sales (12000) is greater than your actual income (4000). This may be an error.",
         |              "Please read our guidance and consider amending Box 10 of your submission.",
         |              "Our guidance on Turnover and Expenses can be read here.",
         |              "https://www.gov.uk/expenses-if-youre-self-employed",
         |              "001"
         |            ]
         |          ]
         |        }
         |      ]
         |    },
         |    {
         |      "identifiers": [
         |        {
         |          "name": "feedbackID",
         |          "value": "123490b4-06e3-4fef-a555-6fd0877dc7ca"
         |        },
         |        {
         |          "name": "calculationID",
         |          "value": "537490b4-06e3-4fef-a555-6fd0877dc7ca"
         |        }
         |      ]
         |    }
         |  ]
         |}
         |
         |""".stripMargin
    ).as("application/json"))
  }
}

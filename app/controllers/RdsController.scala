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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton()
class RdsController @Inject()(cc: ControllerComponents)
  extends BackendController(cc) with Logging{

  val retSubmissionSuccesful =
     s"""
          |{
          |    "links": [],
          |    "version": 2,
          |    "moduleId": "HMRC_ASSIST_ITSA_FINSUB_FEEDBACK",
          |    "stepId": "execute",
          |    "executionState": "completed",
          |    "outputs": [
          |        {
          |            "name": "welshActions",
          |            "value": [
          |                {
          |                    "metadata": [
          |                        {
          |                            "TITLE": "string"
          |                        },
          |                        {
          |                            "MESSAGE": "string"
          |                        },
          |                        {
          |                            "ACTION": "string"
          |                        },
          |                        {
          |                            "LINKTITLE": "string"
          |                        },
          |                        {
          |                            "LINKURL": "string"
          |                        },
          |                        {
          |                            "PATH": "string"
          |                        }
          |                    ]
          |                },
          |                {
          |                    "data": [
          |                        [
          |                            "Ffynhonnell Incwm Di-Fusnes",
          |                            "Rydych wedi datgan benthyciad teulu fel ffynhonnell eich incwm. Bu newidiadau i'r rheolau ynghylch ffynonellau nad ydynt yn ymwneud â busnes y gallwch eu datgan, darllenwch y canllawiau priodol i weld sut mae hyn yn effeithio arnoch chi.",
          |                            "Gwirio Canllawiau",
          |                            "[Canllawiau ITSA, Arweiniad i Ffynonellau Incwm]",
          |                            "[www.itsa/cym.gov.uk, www.itsa/incomesources.gov.uk]",
          |                            "general/non_business_income_sources/income_source"
          |                        ],
          |                        [
          |                            "Trosiant",
          |                            "Mae'n ymddangos bod eich trosiant datganedig o £80,000 yn is na'r disgwyl yn seiliedig ar eich ffynonellau incwm, cadarnhewch y cyfrifir am yr holl drosiant cyn cyflwyno.",
          |                            "Gwiriwch y trosiant",
          |                            "[Cyfrifo am Incwm]",
          |                            "[www.itsa/incomecompliance.gov.uk]",
          |                            "general/total_declared_turnover"
          |                        ]
          |                    ]
          |                }
          |            ]
          |        },
          |        {
          |            "name": "englishActions",
          |            "value": [
          |                {
          |                    "metadata": [
          |                        {
          |                            "TITLE": "string"
          |                        },
          |                        {
          |                            "MESSAGE": "string"
          |                        },
          |                        {
          |                            "ACTION": "string"
          |                        },
          |                        {
          |                            "LINKTITLE": "string"
          |                        },
          |                        {
          |                            "LINKURL": "string"
          |                        },
          |                        {
          |                            "PATH": "string"
          |                        }
          |                    ]
          |                },
          |                {
          |                    "data": [
          |                        [
          |                            "Non-Business Income Source",
          |                            "You have declared family loan as a source of your income. There have been changes to the rules around non-business sources you may declare, please check the appropriate guidance to see how this impacts you.",
          |                            "Check guidance",
          |                            "[ITSA Guidance, Income Source Guidance]",
          |                            "[www.itsa.gov.uk, www.itsa/incomesources.gov.uk]",
          |                            "general/non_business_income_sources/income_source"
          |                        ],
          |                        [
          |                            "Turnover",
          |                            "Your declared turnover of £80,000 appears to be lower than expected based on your income sources, please confirm all turnover is accounted for before submission.",
          |                            "Check turnover",
          |                            "[Accounting for Income]",
          |                            "[www.itsa/incomecompliance.gov.uk]",
          |                            "general/total_declared_turnover"
          |                        ]
          |                    ]
          |                }
          |            ]
          |        },
          |        {
          |      "identifiers": [
          |        {
          |            "name": "feedbackID",
          |            "value": "a365cc12c845c057eb548febfa8048ba"
          |        },
          |        {
          |            "name": "calculationID",
          |            "value": "537490b4-06e3-4fef-a555-6fd0877dc7ca"
          |        },
          |        {
          |            "name": "correlationID",
          |            "value": "5fht738957jfjf845jgjf855"
          |        }
          |        ]
          |       }
          |    ]
          |}
          |
          |""".stripMargin

  val retAcknowledgeReponse = s"""{
                                 |  "links": [],
                                 |  "version": 2,
                                 |  "moduleId": "HMRC_ASSIST_ITSA_FINSUB_FEEDBACK_ACK",
                                 |  "stepId": "execute",
                                 |  "executionState": "completed",
                                 |  "metadata": {
                                 |    "module_id": "HMRC_ASSIST_ITSA_FINSUB_FEEDBACK_ACK",
                                 |    "step_id": "execute"
                                 |  },
                                 |  "outputs": [
                                 |    {
                                 |      "name": "feedbackID",
                                 |      "value": "a365cc12c845c057eb548febfa8048ba"
                                 |    },
                                 |    {
                                 |      "name": "nino",
                                 |      "value": "QQ123456A"
                                 |    },
                                 |    {
                                 |      "name": "taxYear",
                                 |      "value": 2022
                                 |    },
                                 |    {
                                 |      "name": "responseCode",
                                 |      "value": 200
                                 |    },
                                 |    {
                                 |      "name": "response",
                                 |      "value": "String"
                                 |    }
                                 |  ]
                                 |}""".stripMargin

  def generateReport(): Action[JsValue] = Action.async(parse.json) {
    request => {
      logger.info(s"======Invoked RDS for report generation======")
      Future.successful(Ok(Json.parse(retSubmissionSuccesful)))
    }
  }

  def acknowledgeReport(): Action[JsValue] = Action.async(parse.json) {
    request => {
      logger.info(s"======Invoked RDS for report generation======")
      Future.successful(Ok(Json.parse(retAcknowledgeReponse)))
    }
  }
}





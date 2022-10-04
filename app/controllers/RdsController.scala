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

import common.StubResource
import models.{CalculationIdDetails, FeedbackFive, FeedbackForDefaultResponse, FeedbackFour, FeedbackOne, FeedbackThree, FeedbackTwo, RdsRequest}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.io.FileNotFoundException
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton()
class RdsController @Inject()(cc: ControllerComponents)
  extends BackendController(cc) with StubResource {

  //below store is used for generate report to map calculation id to feedback
  val calcIdMappings: Map[String, CalculationIdDetails] = Map(
    FeedbackOne.calculationID -> FeedbackOne,
    FeedbackTwo.calculationID -> FeedbackTwo,
    FeedbackThree.calculationID -> FeedbackThree,
    FeedbackFour.calculationID -> FeedbackFour,
    FeedbackFive.calculationID -> FeedbackFive
  ).withDefaultValue(FeedbackForDefaultResponse)

  //below store is used to find feedback and correlation if mapping while accepting acknowledge request
  val feedbackIDAndCorrelationIDMapping = Map(
    FeedbackOne.feedbackID -> FeedbackOne,
    FeedbackTwo.feedbackID -> FeedbackTwo,
    FeedbackThree.feedbackID -> FeedbackThree,
    FeedbackFour.feedbackID -> FeedbackFour,
    FeedbackFive.feedbackID -> FeedbackFive,
    FeedbackForDefaultResponse.feedbackID -> FeedbackForDefaultResponse
  )

  val error =
    s"""
       |{
       |  "code": "MATCHING_RESOURCE_NOT_FOUND",
       |  "message": "The Calculation ID was not found at this time. You can try again later"
       |  }
       |""".stripMargin

  val invalidBodyError =
    s"""
       |{
       |  "code": "BAD_REQUEST",
       |  "message": "Invalid feedback/correlationid"
       |  }
       |""".stripMargin


  def generateReport(): Action[JsValue] = Action.async(parse.json) {
    request: Request[JsValue] => {
      logger.info(s"======Invoked RDS stub for report generation======")
      val rdsRequestValidationResult = request.body.validate[RdsRequest]

      val statusJson = rdsRequestValidationResult match {
        case JsSuccess(rdsRequest, _) =>
          val calculationIdDetails = calcIdMappings(rdsRequest.calculationId.toString)
          try {
            val response = loadSubmitResponseTemplate(rdsRequest.calculationId.toString, calculationIdDetails.feedbackID, calculationIdDetails.correlationID)
            (200, response)
          } catch {
            case e: FileNotFoundException => (404, Json.parse(error))
          }

        case JsError(errors) => (400, Json.parse(invalidBodyError))
      }

      Future.successful(new Status(statusJson._1)(statusJson._2))
    }
  }

  def acknowledgeReport(): Action[JsValue] = Action.async(parse.json) {
    request => {
      logger.info(s"======Invoked RDS for report acknowledge======")
      val rdsAcknowledgeRequestValidationResult = request.body.validate[RdsRequest]
      val statusJson = rdsAcknowledgeRequestValidationResult match {
        case JsSuccess(rdsRequest, _) =>
          try {
            if (feedbackIDAndCorrelationIDMapping.contains(rdsRequest.feedbackId) &&
              feedbackIDAndCorrelationIDMapping(rdsRequest.feedbackId).correlationID.equals(rdsRequest.correlationId)) {
              val response = loadAckResponseTemplate(rdsRequest.feedbackId, rdsRequest.ninoValue)
              (200, response)
            } else {
              (404, Json.parse(invalidBodyError))
            }
          } catch {
            case e: FileNotFoundException => (404, Json.parse(error))
          }

        case JsError(errors) => (400, Json.parse(invalidBodyError))
      }

      Future.successful(new Status(statusJson._1)(statusJson._2))
    }
  }
}





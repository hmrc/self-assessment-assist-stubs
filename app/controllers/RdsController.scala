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
import models.{CalculationIdDetails, FeedbackFive, FeedbackForDefaultResponse, FeedbackFour, FeedbackInvalidResponse, FeedbackOne, FeedbackThree, FeedbackTwo, RdsRequest}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.http.BadRequestException
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
    FeedbackFive.calculationID -> FeedbackFive,
    FeedbackInvalidResponse.calculationID -> FeedbackInvalidResponse
  ).withDefaultValue(FeedbackForDefaultResponse)

  //below store is used to find feedback and correlation if mapping while accepting acknowledge request
  val feedbackIDAndCorrelationIDMapping = Map(
    FeedbackOne.feedbackID -> FeedbackOne,
    FeedbackTwo.feedbackID -> FeedbackTwo,
    FeedbackThree.feedbackID -> FeedbackThree,
    FeedbackFour.feedbackID -> FeedbackFour,
    FeedbackFive.feedbackID -> FeedbackFive,
    FeedbackInvalidResponse.feedbackID-> FeedbackInvalidResponse,
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
      logger.info(s"content is ${request.body}")
      val rdsRequestValidationResult = request.body.validate[RdsRequest]
      logger.info(s"validation result  is ${rdsRequestValidationResult}")
      val statusJson = rdsRequestValidationResult match {
        case JsSuccess(rdsRequest, _) =>
          val fraudRiskReportReasons = rdsRequest.fraudRiskReportReasons
          val calculationIDDetails = calcIdMappings(rdsRequest.calculationID.toString)
          logger.info(s"checking calculation $calculationIDDetails.calculationID")
          try {
            val response =  if(calculationIDDetails.calculationID.equals(FeedbackInvalidResponse.calculationID)){
                loadSubmitResponseTemplate(replaceFeedbackID=calculationIDDetails.feedbackID, replaceCorrelationID=calculationIDDetails.correlationID)
            }else {
                loadSubmitResponseTemplate(Some(rdsRequest.calculationID.toString), calculationIDDetails.feedbackID, calculationIDDetails.correlationID)
            }
            logger.info(s"sending response as $response")
            (200, response)
          } catch {
            case e: FileNotFoundException => (404, Json.parse(error))
            case b: BadRequestException => (400, Json.parse(invalidBodyError))
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
            val fb = feedbackIDAndCorrelationIDMapping.contains(rdsRequest.feedbackID)
            val feedbackDetails = feedbackIDAndCorrelationIDMapping(rdsRequest.feedbackID)
            val correlationID = feedbackDetails.correlationID
            if  ( fb &&
                  correlationID.equals(rdsRequest.correlationID) ) {
              val response = loadAckResponseTemplate(rdsRequest.feedbackID, rdsRequest.ninoValue, "202")
              ( CREATED , response)
            } else {
              (404, Json.parse(invalidBodyError))
            }
          } catch {
            case e: FileNotFoundException => (404, Json.parse(error))
            case b: BadRequestException => (400, Json.parse(error))
          }

        case JsError(errors) => (400, Json.parse(invalidBodyError))
      }

      Future.successful(new Status(statusJson._1)(statusJson._2))
    }
  }
}





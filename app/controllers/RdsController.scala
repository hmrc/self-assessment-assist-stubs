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
import models.{CalculationIdDetails, FeedbackFive, FeedbackForDefaultResponse, FeedbackFour, FeedbackFromRDS, FeedbackInvalidCalculationId, FeedbackMissingCalculationId, FeedbackOne, FeedbackThree, FeedbackTwo, RdsRequest}
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
    FeedbackOne.calculationId -> FeedbackOne,
    FeedbackTwo.calculationId -> FeedbackTwo,
    FeedbackThree.calculationId -> FeedbackThree,
    FeedbackFour.calculationId -> FeedbackFour,
    FeedbackFive.calculationId -> FeedbackFive,
    FeedbackInvalidCalculationId.calculationId -> FeedbackInvalidCalculationId,
    FeedbackMissingCalculationId.calculationId -> FeedbackMissingCalculationId,
    FeedbackFromRDS.calculationId -> FeedbackFromRDS
  ).withDefaultValue(FeedbackForDefaultResponse)

  //below store is used to find feedback and correlation if mapping while accepting acknowledge request
  val feedbackIdAndCorrelationIdMapping = Map(
    FeedbackOne.feedbackId -> FeedbackOne,
    FeedbackTwo.feedbackId -> FeedbackTwo,
    FeedbackThree.feedbackId -> FeedbackThree,
    FeedbackFour.feedbackId -> FeedbackFour,
    FeedbackFive.feedbackId -> FeedbackFive,
    FeedbackInvalidCalculationId.feedbackId-> FeedbackInvalidCalculationId,
    FeedbackForDefaultResponse.feedbackId -> FeedbackForDefaultResponse,
    FeedbackMissingCalculationId.feedbackId -> FeedbackMissingCalculationId,
    FeedbackFromRDS.calculationId -> FeedbackFromRDS
  )

  val error =
    s"""
       |{
       |  "code": "MATCHING_RESOURCE_NOT_FOUND",
       |  "message": "The Calculation Id was not found at this time. You can try again later"
       |  }
       |""".stripMargin

  val invalidBodyError =
    s"""
       |{
       |  "code": "BAD_REQUEST",
       |  "message": "Invalid feedback/correlationId"
       |  }
       |""".stripMargin


  def generateReport(): Action[JsValue] = Action.async(parse.json) {
    request: Request[JsValue] => {
      logger.info(s"======Invoked RDS stub for report generation======")
      //logger.info(s"content is ${request.body}")
      val rdsRequestValidationResult = request.body.validate[RdsRequest]
      logger.info(s"validation result  is ${rdsRequestValidationResult}")
      val statusJson = rdsRequestValidationResult match {
        case JsSuccess(rdsRequest, _) =>
          val fraudRiskReportReasons = rdsRequest.fraudRiskReportReasons
          val calculationIdDetails = calcIdMappings(rdsRequest.calculationId.toString)

          try {
            val response =
                loadSubmitResponseTemplate(
                  calculationIdDetails.calculationId,
                  calculationIdDetails.feedbackId,
                  calculationIdDetails.correlationId
                )
            logger.info(s"sending response")
            (CREATED, response)
          } catch {
            case e: FileNotFoundException => (NOT_FOUND, Json.parse(error))
            case b: BadRequestException => (BAD_REQUEST, Json.parse(invalidBodyError))
            case _ => (INTERNAL_SERVER_ERROR, Json.parse(error))
          }

        case JsError(errors) => (BAD_REQUEST, Json.parse(invalidBodyError))
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
            val fb = feedbackIdAndCorrelationIdMapping.contains(rdsRequest.feedbackId)
            val feedbackDetails = feedbackIdAndCorrelationIdMapping(rdsRequest.feedbackId)
            val correlationId = feedbackDetails.correlationId
            if  ( fb &&
                  correlationId.equals(rdsRequest.correlationId) ) {
              val response = loadAckResponseTemplate(rdsRequest.feedbackId, rdsRequest.ninoValue, "202")
              ( CREATED , response)
            } else {
              (NOT_FOUND, Json.parse(invalidBodyError))
            }
          } catch {
            case e: FileNotFoundException => (NOT_FOUND, Json.parse(error))
            case b: BadRequestException => (BAD_REQUEST, Json.parse(error))
          }

        case JsError(errors) => (BAD_REQUEST, Json.parse(invalidBodyError))
      }

      Future.successful(new Status(statusJson._1)(statusJson._2))
    }
  }
}





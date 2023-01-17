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

import common.StubResource
import models.{CalculationIdDetails, FeedbackFiveHttp201ResponseCode201, FeedbackForDefaultResponse, FeedbackFourHttp201ResponseCode201, FeedbackFromRDSDevHttp201ResponseCode201, FeedbackHttp201ResponseCode204, FeedbackHttp201ResponseCode404, FeedbackInvalidCalculationId, FeedbackMissingCalculationId, FeedbackOneHttp201ResponseCode201, FeedbackSevenNRSFailureHttp201ResponseCode201, FeedbackThreeHttp201ResponseCode201, FeedbackTwoHttp201ResponseCode201, RdsRequest}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.http.BadRequestException
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.io.FileNotFoundException
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.control.NonFatal

@Singleton()
class RdsController @Inject()(cc: ControllerComponents)
  extends BackendController(cc) with StubResource {

  //below store is used for generate report to map calculation id to feedback
  private val calcIdMappings: Map[String, CalculationIdDetails] = Map(
    FeedbackOneHttp201ResponseCode201.calculationId -> FeedbackOneHttp201ResponseCode201,
    FeedbackTwoHttp201ResponseCode201.calculationId -> FeedbackTwoHttp201ResponseCode201,
    FeedbackThreeHttp201ResponseCode201.calculationId -> FeedbackThreeHttp201ResponseCode201,
    FeedbackFourHttp201ResponseCode201.calculationId -> FeedbackFourHttp201ResponseCode201,
    FeedbackFiveHttp201ResponseCode201.calculationId -> FeedbackFiveHttp201ResponseCode201,
    FeedbackInvalidCalculationId.calculationId -> FeedbackInvalidCalculationId,
    FeedbackMissingCalculationId.calculationId -> FeedbackMissingCalculationId,
    FeedbackFromRDSDevHttp201ResponseCode201.calculationId -> FeedbackFromRDSDevHttp201ResponseCode201,
    FeedbackHttp201ResponseCode204.calculationId -> FeedbackHttp201ResponseCode204,
    FeedbackHttp201ResponseCode404.calculationId -> FeedbackHttp201ResponseCode404,
    FeedbackSevenNRSFailureHttp201ResponseCode201.calculationId -> FeedbackSevenNRSFailureHttp201ResponseCode201

  ).withDefaultValue(FeedbackForDefaultResponse)

  //below store is used to find feedback and correlation if mapping while accepting acknowledge request
  private val feedbackIdAndCorrelationIdMapping: Map[String, CalculationIdDetails] = Map(
    FeedbackOneHttp201ResponseCode201.feedbackId -> FeedbackOneHttp201ResponseCode201,
    FeedbackTwoHttp201ResponseCode201.feedbackId -> FeedbackTwoHttp201ResponseCode201,
    FeedbackThreeHttp201ResponseCode201.feedbackId -> FeedbackThreeHttp201ResponseCode201,
    FeedbackFourHttp201ResponseCode201.feedbackId -> FeedbackFourHttp201ResponseCode201,
    FeedbackFiveHttp201ResponseCode201.feedbackId -> FeedbackFiveHttp201ResponseCode201,
    FeedbackInvalidCalculationId.feedbackId-> FeedbackInvalidCalculationId,
    FeedbackForDefaultResponse.feedbackId -> FeedbackForDefaultResponse,
    FeedbackMissingCalculationId.feedbackId -> FeedbackMissingCalculationId,
    FeedbackFromRDSDevHttp201ResponseCode201.feedbackId -> FeedbackFromRDSDevHttp201ResponseCode201,
    FeedbackHttp201ResponseCode204.feedbackId -> FeedbackHttp201ResponseCode204,
    FeedbackHttp201ResponseCode404.feedbackId -> FeedbackHttp201ResponseCode404,
    FeedbackSevenNRSFailureHttp201ResponseCode201.feedbackId -> FeedbackSevenNRSFailureHttp201ResponseCode201
  )

  private val error: String =
    s"""
       |{
       |  "code": "MATCHING_RESOURCE_NOT_FOUND",
       |  "message": "The Calculation Id was not found at this time. You can try again later"
       |  }
       |""".stripMargin

  private val invalidBodyError: String =
    s"""
       |{
       |  "code": "BAD_REQUEST",
       |  "message": "Invalid feedback/correlationId"
       |  }
       |""".stripMargin

  private val rdsNotAvailableError: String =
    s"""
       |{
       |  "code": "NOT_FOUND",
       |  "message": "Scenario to mimic non availabilty of RDS"
       |  }
       |""".stripMargin

  private val rdsRequestTimeoutError: String =
    s"""
       |{
       |  "code": "REQUEST_TIMEOUT",
       |  "message": "RDS request timeout"
       |  }
       |""".stripMargin

  def generateReport(): Action[JsValue] = Action.async(parse.json) {
    request: Request[JsValue] => {
      logger.info(s"======Invoked RDS stub for report generation======")
      val rdsRequestValidationResult = request.body.validate[RdsRequest]
      val statusJson = rdsRequestValidationResult match {
        case JsSuccess(rdsRequest, _) =>
          val fraudRiskReportReasons = rdsRequest.fraudRiskReportReasons
          rdsRequest.calculationId.toString match {
            case "640090b4-06e3-4fef-a555-6fd0877dc7ca" => (BAD_REQUEST,Json.parse(invalidBodyError))
            case "404404b4-06e3-4fef-a555-6fd0877dc7ca" => (NOT_FOUND,Json.parse(rdsNotAvailableError))
            case "408408b4-06e3-4fef-a555-6fd0877dc7ca" => (REQUEST_TIMEOUT,Json.parse(rdsNotAvailableError))
            case _ =>
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
                case _: FileNotFoundException => (NOT_FOUND, Json.parse(error))
                case _: BadRequestException => (BAD_REQUEST, Json.parse(invalidBodyError))
                case NonFatal(_) => (INTERNAL_SERVER_ERROR, Json.parse(error))
              }
          }

        case JsError(_) => (BAD_REQUEST, Json.parse(invalidBodyError))
      }

      Future.successful(new Status(statusJson._1)(statusJson._2))
    }
  }

  def acknowledgeReport(): Action[JsValue] = Action.async(parse.json) {
    request => {
      logger.info(s"======Invoked RDS for report acknowledge======")
      val rdsAcknowledgeRequestValidationResult = request.body.validate[RdsRequest]
      logger.info(s"======validation success======")
      val statusJson = rdsAcknowledgeRequestValidationResult match {
        case JsSuccess(rdsRequest, _) =>
          try {
            logger.info(s"====== success path======")
            val fb = feedbackIdAndCorrelationIdMapping.contains(rdsRequest.feedbackId)
            val feedbackDetails = feedbackIdAndCorrelationIdMapping(rdsRequest.feedbackId)
            val correlationId = feedbackDetails.correlationId
            if  ( fb &&
                  correlationId.equals(rdsRequest.correlationId) ) {
              val response = loadAckResponseTemplate(rdsRequest.feedbackId, rdsRequest.ninoValue, "202")
              ( CREATED , response)
            } else {
              logger.info(s"====== returning not found ======")
              (BAD_REQUEST, Json.parse(invalidBodyError))
            }
          } catch {
            case e: FileNotFoundException =>
              logger.error(s"======FileNotFoundException $e======")
              (NOT_FOUND, Json.parse(error))
            case b: BadRequestException =>
              logger.error(s"======BadRequestException $b======")
              (BAD_REQUEST, Json.parse(error))
          }

        case JsError(_) => (BAD_REQUEST, Json.parse(invalidBodyError))
      }

      Future.successful(new Status(statusJson._1)(statusJson._2))
    }
  }
}





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
import models.{FeedbackForBadRequest, RdsInternalServerError500, RdsNotAvailable404, RdsRequest, RdsServiceNotAvailable503, RdsTimeout408}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.http.BadRequestException
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.CommonData.{calcIdMappings, feedbackIdAndCorrelationIdMapping}

import java.io.FileNotFoundException
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.control.NonFatal

@Singleton()
class RdsController @Inject()(cc: ControllerComponents)
  extends BackendController(cc) with StubResource {



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
       |  "code": "FORBIDDEN",
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

  private val rdsInternalServerError: String =
    s"""
       |{
       |  "code": "INTERNAL_SERVER_ERROR",
       |  "message": "RDS internal server error"
       |  }
       |""".stripMargin

  private val rdsServiceUnvailableError: String =
    s"""
       |{
       |  "code": "SERVICE_UNAVAIALBLE",
       |  "message": "RDS service not available error"
       |  }
       |""".stripMargin

  def generateReport(): Action[JsValue] = Action.async(parse.json) {
    request: Request[JsValue] => {
      logger.info(s"======Invoked RDS stub for report generation======")
      val rdsRequestValidationResult = request.body.validate[RdsRequest]
      @annotation.nowarn
      val statusJson = rdsRequestValidationResult match {
        case JsSuccess(rdsRequest, _) =>
          val fraudRiskReportReasons = rdsRequest.fraudRiskReportReasons
          rdsRequest.calculationId.toString match {
            case FeedbackForBadRequest.calculationId => (BAD_REQUEST,Json.parse(invalidBodyError))
            case RdsNotAvailable404.calculationId => (NOT_FOUND,Json.parse(rdsNotAvailableError))
            case RdsTimeout408.calculationId => (REQUEST_TIMEOUT,Json.parse(rdsRequestTimeoutError))
            case RdsInternalServerError500.calculationId => (INTERNAL_SERVER_ERROR,Json.parse(rdsInternalServerError))
            case RdsServiceNotAvailable503.calculationId => (SERVICE_UNAVAILABLE,Json.parse(rdsServiceUnvailableError))
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
            def feedbackDetails = feedbackIdAndCorrelationIdMapping(rdsRequest.feedbackId)

            (feedbackDetails.feedbackId,feedbackDetails.correlationId) match {
              case(FeedbackForBadRequest.feedbackId,_)      => (BAD_REQUEST, Json.parse(invalidBodyError))
              case(RdsNotAvailable404.feedbackId,_)         => (NOT_FOUND, Json.parse(rdsNotAvailableError))
              case(RdsTimeout408.feedbackId,_)              => (REQUEST_TIMEOUT, Json.parse(rdsRequestTimeoutError))
              case(RdsInternalServerError500.feedbackId,_)  => (INTERNAL_SERVER_ERROR, Json.parse(rdsInternalServerError))
              case(RdsServiceNotAvailable503.feedbackId,_)  => (SERVICE_UNAVAILABLE, Json.parse(rdsServiceUnvailableError))
              case (feedbackId,correlationId) if (rdsRequest.feedbackId.equals(feedbackId) && rdsRequest.correlationId.equals(correlationId))=>
                val response = loadAckResponseTemplate(rdsRequest.feedbackId, rdsRequest.ninoValue, "202",s"conf/response/acknowledge/feedback-ack.json")
                (CREATED, response)
              case(_,_)                                 =>
                  logger.info(s"====== combination not found ======")
                  val fileName = s"conf/response/acknowledge/ack-resp-invalid-report-correlationid-combination.json"
                  val response = loadAckResponseTemplate(rdsRequest.feedbackId, rdsRequest.ninoValue, "401",fileName)
                  (CREATED, response)
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





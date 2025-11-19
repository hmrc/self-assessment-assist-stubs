/*
 * Copyright 2025 HM Revenue & Customs
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
import models._
import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, ControllerComponents, Request, Result}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.CommonData.{calcIdMappings, feedbackIdAndCorrelationIdMapping}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.Random.alphanumeric
import scala.util.{Failure, Success, Try}

@Singleton
class RdsController @Inject()(cc: ControllerComponents, stubResource: StubResource) extends BackendController(cc) with Logging {

  val calcIdNotFoundError: JsValue = Json.parse(
    """
      |{
      |  "code": "MATCHING_RESOURCE_NOT_FOUND",
      |  "message": "The Calculation Id was not found at this time. You can try again later"
      |}
    """.stripMargin
  )

  val invalidBodyError: JsValue = Json.parse(
    """
      |{
      |  "code": "FORBIDDEN",
      |  "message": "Invalid request"
      |}
    """.stripMargin
  )

  def requestValidationFailure(msg: String): JsValue = Json.parse(
    s"""
      |{
      |  "code": "FORBIDDEN",
      |  "message": "$msg"
      |}
    """.stripMargin
  )

  val rdsNotAvailableError: JsValue = Json.parse(
    """
      |{
      |  "code": "NOT_FOUND",
      |  "message": "Scenario to mimic non availability of RDS"
      |}
    """.stripMargin
  )

  val rdsRequestTimeoutError: JsValue = Json.parse(
    """
      |{
      |  "code": "REQUEST_TIMEOUT",
      |  "message": "RDS request timeout"
      |}
    """.stripMargin
  )

  val rdsInternalServerError: JsValue = Json.parse(
    """
      |{
      |  "code": "INTERNAL_SERVER_ERROR",
      |  "message": "RDS internal server error"
      |}
    """.stripMargin
  )

  val rdsServiceUnavailableError: JsValue = Json.parse(
    """
      |{
      |  "code": "SERVICE_UNAVAILABLE",
      |  "message": "RDS service not available error"
      |}
    """.stripMargin
  )

  private def loadTemplate(loadFn: => JsValue, method: String, isSandboxMode: Boolean): Result = {
    Try(loadFn) match {
      case Success(response: JsValue) =>
        val logMessage: String = if (isSandboxMode) {
          s"[RdsController][$method] Sandbox mode enabled - returning success response"
        } else {
          s"[RdsController][$method] Returning success response"
        }
        logger.info(logMessage)
        Created(response)
      case Failure(_) => InternalServerError(calcIdNotFoundError)
    }
  }

  private val existingFeedbackIds: Set[String] = feedbackIdAndCorrelationIdMapping.keySet

  def sandboxFeedbackId: String = Iterator.continually(UUID.randomUUID().toString).dropWhile(existingFeedbackIds.contains).next()

  def sandboxCorrelationId: String = alphanumeric.take(64).mkString.toUpperCase

  def generateReport(): Action[JsValue] = Action.async(parse.json) { request: Request[JsValue] =>
    logger.info("[RdsController][generateReport] Invoked RDS for report generation")

    val result: Result = request.body.validate[RdsRequest] match {
      case JsError(_) => BadRequest(invalidBodyError)
      case JsSuccess(rdsRequest, _) if !rdsRequest.isValid._1 => BadRequest(requestValidationFailure(rdsRequest.isValid._2))
      case JsSuccess(rdsRequest, _) =>
        val calculationId: String = rdsRequest.calculationId.toString

        if (stubResource.isSandboxMode) {
          loadTemplate(
            loadFn = stubResource.loadSubmitResponseTemplate(calculationId, sandboxFeedbackId, sandboxCorrelationId),
            method = "generateReport",
            isSandboxMode = true
          )
        } else {
          val details: CalculationIdDetails = calcIdMappings(calculationId)

          val (detailsFeedbackId, detailsCorrelationId): (String, String) = (details.feedbackId, details.correlationId)

          calculationId match {
            case FeedbackForBadRequest.calculationId => BadRequest(invalidBodyError)
            case RdsNotAvailable404.calculationId => NotFound(rdsNotAvailableError)
            case RdsTimeout408.calculationId => RequestTimeout(rdsRequestTimeoutError)
            case RdsInternalServerError500.calculationId => InternalServerError(rdsInternalServerError)
            case RdsServiceNotAvailable503.calculationId => ServiceUnavailable(rdsServiceUnavailableError)
            case _ => loadTemplate(
              loadFn = stubResource.loadSubmitResponseTemplate(calculationId, detailsFeedbackId, detailsCorrelationId),
              method = "generateReport",
              isSandboxMode = false
            )
          }
        }
    }

    Future.successful(result)
  }

  def acknowledgeReport(): Action[JsValue] = Action.async(parse.json) { request: Request[JsValue] =>
    logger.info("[RdsController][acknowledgeReport] Invoked RDS for report acknowledgement")

    val result: Result = request.body.validate[RdsRequest] match {
      case JsError(_) => BadRequest(invalidBodyError)
      case JsSuccess(rdsRequest, _) =>
        val (requestFeedbackId, requestCorrelationId, requestNino): (String, String, String) =
          (rdsRequest.feedbackId, rdsRequest.correlationId, rdsRequest.ninoValue)

        val details: CalculationIdDetails = feedbackIdAndCorrelationIdMapping(requestFeedbackId)

        val (detailsFeedbackId, detailsCorrelationId): (String, String) = (details.feedbackId, details.correlationId)

        val successAckResponseFile: String = "conf/response/acknowledge/feedback-ack-202.json"
        val invalidAckResponseFile: String = "conf/response/acknowledge/ack-resp-invalid-report-correlationid-combination.json"

        if (stubResource.isSandboxMode) {
          loadTemplate(
            loadFn = stubResource.loadAckResponseTemplate(requestFeedbackId, requestNino, successAckResponseFile),
            method = "acknowledgeReport",
            isSandboxMode = true
          )
        } else {
          (requestFeedbackId, requestCorrelationId) match {
            case (FeedbackForBadRequest.feedbackId, _) => BadRequest(invalidBodyError)
            case (RdsNotAvailable404.feedbackId, _) => NotFound(rdsNotAvailableError)
            case (RdsTimeout408.feedbackId, _) => RequestTimeout(rdsRequestTimeoutError)
            case (RdsInternalServerError500.feedbackId, _) => InternalServerError(rdsInternalServerError)
            case (RdsServiceNotAvailable503.feedbackId, _) => ServiceUnavailable(rdsServiceUnavailableError)
            case (`detailsFeedbackId`, `detailsCorrelationId`) => loadTemplate(
              loadFn = stubResource.loadAckResponseTemplate(requestFeedbackId, requestNino, successAckResponseFile),
              method = "acknowledgeReport",
              isSandboxMode = false
            )
            case _ =>
              logger.info("[RdsController][acknowledgeReport] Combination not found")
              loadTemplate(
                loadFn = stubResource.loadAckResponseTemplate(requestFeedbackId, requestNino, invalidAckResponseFile),
                method = "acknowledgeReport",
                isSandboxMode = false
              )
          }
        }
    }

    Future.successful(result)
  }
}

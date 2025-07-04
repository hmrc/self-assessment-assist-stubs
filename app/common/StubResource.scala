/*
 * Copyright 2024 HM Revenue & Customs
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

package common

import models.{FeedbackForBadRequest, NrsAccepted, NrsBadGateway, NrsBadRequest, NrsChecksumFailed, NrsGatewayTimeout, NrsInternalServerError, NrsNetworkTimeout, NrsNotFound, NrsServiceUnavailable, NrsUnauthorised, RdsInvalidRespWithMissingCalculationId}
import play.api.Logging
import play.api.http.ContentTypes
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results

import java.io.{File, FileInputStream}

trait StubResource extends Results with ContentTypes with Logging {

  def loadSubmitResponseTemplate(calculationId:String, replaceFeedbackId: String, replaceCorrelationId: String): JsValue = {
    val templateCotent =
      calculationId match {
        case calcId@(FeedbackForBadRequest.calculationId |
                     RdsInvalidRespWithMissingCalculationId.calculationId)=>
          logger.info(s"loading invalid file scenario $calcId")
          findResource(s"conf/response/submit/$calcId-response.json")map(
          _.replace("replaceFeedbackId", replaceFeedbackId)
            .replace("replaceCorrelationId", replaceCorrelationId))

        case calcId@(NrsBadRequest.calculationId | NrsInternalServerError.calculationId |NrsBadGateway.calculationId |
                     NrsServiceUnavailable.calculationId | NrsGatewayTimeout.calculationId
                     |NrsAccepted.calculationId | NrsUnauthorised.calculationId | NrsChecksumFailed.calculationId
                      |NrsNotFound.calculationId | NrsNetworkTimeout.calculationId)=>
          logger.info(s"nrs error scenario $calcId")
          findResource(s"conf/response/submit/nrserrortemplate-response.json")map(
            _.replace("replaceFeedbackId", replaceFeedbackId)
              .replace("replaceCalculationId", calcId)
              .replace("replaceCorrelationId", replaceCorrelationId))

        case calcId@_ =>
          logger.info(s"loading file $calcId")
          findResource(s"conf/response/submit/$calcId-response.json")map(
            _.replace("replaceFeedbackId", replaceFeedbackId)
              .replace("replaceCalculationId", calcId)
              .replace("replaceCorrelationId", replaceCorrelationId))
      }




    val parsedContent = templateCotent
      .map(Json.parse).getOrElse(throw new IllegalStateException("Response template parsing failed"))
    parsedContent
  }

  def loadAckResponseTemplate(replaceFeedbackId: String, replaceNino: String, fileName:String): JsValue = {
    val templateCotent =
      findResource(fileName).map(
        _.replace("replaceFeedbackId", replaceFeedbackId)
          .replace("replaceNino", replaceNino))

    val parsedContent = templateCotent
      .map(Json.parse)
      .getOrElse(throw new IllegalStateException("Acknowledge template parsing failed"))
    parsedContent
  }

  private def findResource(path: String): Option[String] = {
    val file = new File(path)
    val absolutePath = file.getAbsolutePath
    val stream = new FileInputStream(absolutePath)
    val json = try {
      Json.parse(stream)
    } finally {
      stream.close()
    }
    Some(json.toString)
  }
}

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

package utils

import models.{CalculationIdDetails, FeedbackAcknowledgeForbiddenHttp201ResponseCode401, FeedbackFiveHttp201ResponseCode201, FeedbackForDefaultResponse, FeedbackFourHttp201ResponseCode201, FeedbackFromRDSDevHttp201ResponseCode201, FeedbackHttp201ResponseCode204, FeedbackHttp201ResponseCode404, FeedbackMissingCalculationId, FeedbackOneHttp201ResponseCode201, FeedbackForBadRequest, FeedbackSevenNRSFailureHttp201ResponseCode201, FeedbackThreeHttp201ResponseCode201, FeedbackTwoHttp201ResponseCode201, RdsNotAvailable404, RdsTimeout408}

object CommonData {
  val ninoMtdIdPairs = Map(
    "NJ070957A" -> "123456789012345",
    "MS475730B" -> "XCIT00840041559",
    "WS504231C" -> "XBIT00219774624",
    "XT181899C" -> "XDIT00734159815",
    "JL530692C" -> "XQIT00731178134",
    "AA088213C" -> "XQIT00731172134"
  )

  //below store is used for generate report to map calculation id to feedback
   val calcIdMappings: Map[String, CalculationIdDetails] = Map(
    FeedbackOneHttp201ResponseCode201.calculationId -> FeedbackOneHttp201ResponseCode201,
    FeedbackTwoHttp201ResponseCode201.calculationId -> FeedbackTwoHttp201ResponseCode201,
    FeedbackThreeHttp201ResponseCode201.calculationId -> FeedbackThreeHttp201ResponseCode201,
    FeedbackFourHttp201ResponseCode201.calculationId -> FeedbackFourHttp201ResponseCode201,
    FeedbackFiveHttp201ResponseCode201.calculationId -> FeedbackFiveHttp201ResponseCode201,
    FeedbackForBadRequest.calculationId -> FeedbackForBadRequest,
    FeedbackMissingCalculationId.calculationId -> FeedbackMissingCalculationId,
    FeedbackFromRDSDevHttp201ResponseCode201.calculationId -> FeedbackFromRDSDevHttp201ResponseCode201,
    FeedbackHttp201ResponseCode204.calculationId -> FeedbackHttp201ResponseCode204,
    FeedbackHttp201ResponseCode404.calculationId -> FeedbackHttp201ResponseCode404,
    FeedbackSevenNRSFailureHttp201ResponseCode201.calculationId -> FeedbackSevenNRSFailureHttp201ResponseCode201,
     RdsNotAvailable404.calculationId -> RdsNotAvailable404,
     RdsTimeout408.calculationId -> RdsTimeout408,
     FeedbackAcknowledgeForbiddenHttp201ResponseCode401.calculationId -> FeedbackAcknowledgeForbiddenHttp201ResponseCode401
  ).withDefaultValue(FeedbackForDefaultResponse)

  //below store is used to find feedback and correlation if mapping while accepting acknowledge request
   val feedbackIdAndCorrelationIdMapping: Map[String, CalculationIdDetails] = Map(
    FeedbackOneHttp201ResponseCode201.feedbackId -> FeedbackOneHttp201ResponseCode201,
    FeedbackTwoHttp201ResponseCode201.feedbackId -> FeedbackTwoHttp201ResponseCode201,
    FeedbackThreeHttp201ResponseCode201.feedbackId -> FeedbackThreeHttp201ResponseCode201,
    FeedbackFourHttp201ResponseCode201.feedbackId -> FeedbackFourHttp201ResponseCode201,
    FeedbackFiveHttp201ResponseCode201.feedbackId -> FeedbackFiveHttp201ResponseCode201,
    FeedbackForBadRequest.feedbackId-> FeedbackForBadRequest,
    FeedbackMissingCalculationId.feedbackId -> FeedbackMissingCalculationId,
    FeedbackFromRDSDevHttp201ResponseCode201.feedbackId -> FeedbackFromRDSDevHttp201ResponseCode201,
    FeedbackHttp201ResponseCode204.feedbackId -> FeedbackHttp201ResponseCode204,
    FeedbackHttp201ResponseCode404.feedbackId -> FeedbackHttp201ResponseCode404,
    FeedbackSevenNRSFailureHttp201ResponseCode201.feedbackId -> FeedbackSevenNRSFailureHttp201ResponseCode201,
     RdsNotAvailable404.feedbackId -> RdsNotAvailable404,
     RdsTimeout408.feedbackId -> RdsTimeout408,
     FeedbackAcknowledgeForbiddenHttp201ResponseCode401.feedbackId -> FeedbackAcknowledgeForbiddenHttp201ResponseCode401
  ).withDefaultValue(FeedbackForDefaultResponse)
}

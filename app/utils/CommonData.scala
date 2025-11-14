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

package utils

import models._

object CommonData {

  //below store is used for generate report to map calculation id to feedback
   val calcIdMappings: Map[String, CalculationIdDetails] = Map(
     FeedbackForBadRequest.calculationId -> FeedbackForBadRequest,
     RdsInvalidRespWithMissingCalculationId.calculationId -> RdsInvalidRespWithMissingCalculationId,
     FeedbackHttp201ResponseCode204.calculationId -> FeedbackHttp201ResponseCode204,
     FeedbackHttp201ResponseCode404.calculationId -> FeedbackHttp201ResponseCode404,
     RdsNotAvailable404.calculationId -> RdsNotAvailable404,
     RdsTimeout408.calculationId -> RdsTimeout408,
     RdsInternalServerError500.calculationId -> RdsInternalServerError500,
     RdsServiceNotAvailable503.calculationId -> RdsServiceNotAvailable503,
     NrsBadRequest.calculationId -> NrsBadRequest,
     NrsInternalServerError.calculationId -> NrsInternalServerError,
     NrsBadGateway.calculationId -> NrsBadGateway,
     NrsServiceUnavailable.calculationId -> NrsServiceUnavailable,
     NrsGatewayTimeout.calculationId -> NrsGatewayTimeout,
     NrsAccepted.calculationId -> NrsAccepted,
     NrsUnauthorised.calculationId -> NrsUnauthorised,
     NrsChecksumFailed.calculationId -> NrsChecksumFailed,
     NrsNotFound.calculationId -> NrsNotFound,
     NrsNetworkTimeout.calculationId -> NrsNetworkTimeout,
     IfsServiceBadRequest400.calculationId -> IfsServiceBadRequest400,
     IfsServiceRequestTimeout408.calculationId -> IfsServiceRequestTimeout408,
     IfsInternalServerError500.calculationId -> IfsInternalServerError500,
     IfsServiceNotAvailable503.calculationId -> IfsServiceNotAvailable503
  ).withDefaultValue(FeedbackForDefaultResponse)

  //below store is used to find feedback and correlation if mapping while accepting acknowledge request
   val feedbackIdAndCorrelationIdMapping: Map[String, CalculationIdDetails] = Map(
     FeedbackForBadRequest.feedbackId -> FeedbackForBadRequest,
     RdsInvalidRespWithMissingCalculationId.feedbackId -> RdsInvalidRespWithMissingCalculationId,
     FeedbackHttp201ResponseCode204.feedbackId -> FeedbackHttp201ResponseCode204,
     FeedbackHttp201ResponseCode404.feedbackId -> FeedbackHttp201ResponseCode404,
     RdsNotAvailable404.feedbackId -> RdsNotAvailable404,
     RdsTimeout408.feedbackId -> RdsTimeout408,
     RdsInternalServerError500.feedbackId -> RdsInternalServerError500,
     RdsServiceNotAvailable503.feedbackId -> RdsServiceNotAvailable503,
     NrsBadRequest.feedbackId -> NrsBadRequest,
     NrsInternalServerError.feedbackId -> NrsInternalServerError,
     NrsBadGateway.feedbackId -> NrsBadGateway,
     NrsServiceUnavailable.feedbackId -> NrsServiceUnavailable,
     NrsGatewayTimeout.feedbackId -> NrsGatewayTimeout,
     NrsAccepted.feedbackId -> NrsAccepted,
     NrsUnauthorised.feedbackId -> NrsUnauthorised,
     NrsChecksumFailed.feedbackId -> NrsChecksumFailed,
     NrsNotFound.feedbackId -> NrsNotFound,
     NrsNetworkTimeout.feedbackId -> NrsNetworkTimeout,
     IfsServiceBadRequest400.feedbackId -> IfsServiceBadRequest400,
     IfsServiceRequestTimeout408.feedbackId -> IfsServiceRequestTimeout408,
     IfsInternalServerError500.feedbackId -> IfsInternalServerError500,
     IfsServiceNotAvailable503.feedbackId -> IfsServiceNotAvailable503
  ).withDefaultValue(FeedbackForDefaultResponse)
}

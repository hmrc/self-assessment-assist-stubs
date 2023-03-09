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

package models

trait CalculationIdDetails {
  val calculationId: String
  val feedbackId: String
  val correlationId: String
}

case object FeedbackOneHttp201ResponseCode201 extends CalculationIdDetails {
  override val calculationId = "111190b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "a365c0b4-06e3-4fef-a555-16fd0877dc7c"
  override val correlationId = "a5fht738957jfjf845jgjf855"
}

case object FeedbackTwoHttp201ResponseCode201 extends CalculationIdDetails {
  override val calculationId = "222290b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "579800fe-e047-cd40-b3e4-0e14b1f183a8"
  override val correlationId = "b5fht738957jfjf845jgjf855"
}

case object FeedbackThreeHttp201ResponseCode201 extends CalculationIdDetails {
  override val calculationId = "333390b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "c365c0b4-06e3-4fef-a555-16fd0877dc7c"
  override val correlationId = "c5fht738957jfjf845jgjf855"
}

case object FeedbackFourHttp201ResponseCode201 extends CalculationIdDetails {
  override val calculationId = "444490b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "d365c0b4-06e3-4fef-a555-16fd0877dc7c"
  override val correlationId = "d5fht738957jfjf845jgjf855"
}

case object FeedbackFiveHttp201ResponseCode201 extends CalculationIdDetails {
  override val calculationId = "555590b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "e365c0b4-06e3-4fef-a555-16fd0877dc7c"
  override val correlationId = "efht738957jfjf845jgjf855"
}

case object FeedbackForDefaultResponse extends CalculationIdDetails {
  override val calculationId = "000090b4-06e3-4fef-a555-6fd0877dc7de"
  override val feedbackId = "a465c0b4-06e3-4fef-a555-16fd0877dc7c"
  override val correlationId = "g5fht738957jfjf845jgjf855"
}

case object FeedbackForBadRequest extends CalculationIdDetails {
  override val calculationId = "640090b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "579800fe-06e3-4fef-a555-16fd0877dc7c"
  override val correlationId = "efht938957jfjf845jgjf999"
}

case object FeedbackMissingCalculationId extends CalculationIdDetails {
  override val calculationId = "101090b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "a565c0b4-06e3-4fef-a555-16fd0877dc7c"
  override val correlationId = "mfht938957jfjf845jgjf999"
}

case object FeedbackFromRDSDevHttp201ResponseCode201 extends CalculationIdDetails {
  override val calculationId = "666690b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "b365c0b4-06e3-4fef-a555-16fd0877dc7c"
  override val correlationId = "mfht938957jfjf845jgjf999"
}

case object FeedbackHttp201ResponseCode204 extends CalculationIdDetails {
  override val calculationId = "620490b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "c204c0b4-06e3-4fef-a555-16fd0877dc7c"
  override val correlationId = "mfht938957jfjf845jgjf999"
}

case object FeedbackHttp201ResponseCode404 extends CalculationIdDetails {
  override val calculationId = "640490b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "c404c0b4-06e3-4fef-a555-16fd0877dc7c"
  override val correlationId = "mfht938957jfjf845jgjf999"
}

case object FeedbackSevenNRSFailureHttp201ResponseCode201 extends CalculationIdDetails {
  override val calculationId = "777790b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "a365c0b4-06e3-4fef-a555-16fd08770500"
  override val correlationId = "a5fht738957jfjf845jgjf777"
}

case object FeedbackAcknowledgeForbiddenHttp201ResponseCode401 extends CalculationIdDetails {
  override val calculationId = "740190b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "c204c0b4-06e3-4fef-a555-16fd0401dc7c"
  override val correlationId = "a5fht738957jfjf845jgjf777"
}

case object RdsNotAvailable404 extends CalculationIdDetails {
  override val calculationId = "404404b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "c204c0b4-06e3-4fef-a555-16fd0404dc7c"
  override val correlationId = "a5fht738957jfjf845jgjf777"
}

case object RdsTimeout408 extends CalculationIdDetails {
  override val calculationId = "408408b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "f065r0b4-06e3-4fef-a555-16fd04080500"
  override val correlationId = "a5fht738957jfjf845jgjf777"
}

case object RdsInternalServerError500 extends CalculationIdDetails {
  override val calculationId = "500500b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "f065r0b4-06e3-4fef-a555-16fd05000500"
  override val correlationId = "a5fht738957jfjf500jgjf777"
}

case object RdsServiceNotAvailable503 extends CalculationIdDetails {
  override val calculationId = "503503b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "f065r0b4-06e3-4fef-a555-16fd05030500"
  override val correlationId = "a5fht738957jfjf503jgjf777"
}

case object NrsBadRequest extends CalculationIdDetails {
  override val calculationId = "503503b4-06e3-4fef-a555-6fd0877dc7ba"
  override val feedbackId = "a365c0b4-06e3-4fef-a555-16fd08770400"
  override val correlationId = "a5fht738957jfjf503jgjf777"
}
case object NrsInternalServerError extends CalculationIdDetails {
  override val calculationId = "503503b4-06e3-4fef-a555-6fd0877dc7ba"
  override val feedbackId = "a365c0b4-06e3-4fef-a555-16fd08775500"
  override val correlationId = "a5fht738957jfjf503jgjf777"
}
case object NrsBadGateway extends CalculationIdDetails {
  override val calculationId = "503503b4-06e3-4fef-a555-6fd0877dc7ba"
  override val feedbackId = "a365c0b4-06e3-4fef-a555-16fd08770502"
  override val correlationId = "a5fht738957jfjf503jgjf777"
}
case object NrsServiceUnavailable extends CalculationIdDetails {
  override val calculationId = "503503b4-06e3-4fef-a555-6fd0877dc7ba"
  override val feedbackId = "a365c0b4-06e3-4fef-a555-16fd08770503"
  override val correlationId = "a5fht738957jfjf503jgjf777"
}
case object NrsGatewayTimeout extends CalculationIdDetails {
  override val calculationId = "503503b4-06e3-4fef-a555-6fd0877dc7ba"
  override val feedbackId = "a365c0b4-06e3-4fef-a555-16fd08770504"
  override val correlationId = "a5fht738957jfjf503jgjf777"
}
case object NrsAccepted extends CalculationIdDetails {
  override val calculationId = "503503b4-06e3-4fef-a555-6fd0877dc7ba"
  override val feedbackId = "a365c0b4-06e3-4fef-a555-16fd08770202"
  override val correlationId = "a5fht738957jfjf503jgjf777"
}

case object IfsServiceBadRequest400 extends CalculationIdDetails {
  override val calculationId = "400500b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "a365c0b4-06e3-4fef-a400-16fd08770202"
  override val correlationId = "a5fht738957jfjf500jgjf777"
}

case object IfsServiceRequestTimeout408 extends CalculationIdDetails {
  override val calculationId = "408503b4-06e3-4fef-a555-5fd0877dc7ca"
  override val feedbackId = "a365c0b4-06e3-4fef-a408-16fd08770202"
  override val correlationId = "a5fht738957jfjf503jgjf778"
}

case object IfsInternalServerError500 extends CalculationIdDetails {
  override val calculationId = "500500b4-06e3-4fef-a555-4fd0877dc7ca"
  override val feedbackId = "a365c0b4-06e3-4fef-a500-16fd08770202"
  override val correlationId = "a5fht738957jfjf500jgjf779"
}

case object IfsServiceNotAvailable503 extends CalculationIdDetails {
  override val calculationId = "503503b4-06e3-4fef-a555-3fd0877dc7ca"
  override val feedbackId = "a365c0b4-06e3-4fef-a503-16fd08770202"
  override val correlationId = "a8fht738957jfjf503jgjf777"
}
case object IfsServiceInternalServiceError500 extends CalculationIdDetails {
  override val calculationId = "500203b4-06e3-4fef-a555-3fd0877dc7ca"
  override val feedbackId = "a500c0b4-06e3-4fef-a500-16fd08770202"
  override val correlationId = "a5fht738957jfjf503jgjf677"
}
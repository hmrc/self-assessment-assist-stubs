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

package models

trait CalculationIdDetails {
  val calculationId: String
  val feedbackId: String
  val correlationId: String
}

case object FeedbackOne extends CalculationIdDetails {
  override val calculationId = "111190b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "a365c0b4-06e3-4fef-a555-16fd0877dc7c"
  override val correlationId = "a5fht738957jfjf845jgjf855"
}

case object FeedbackTwo extends CalculationIdDetails {
  override val calculationId = "222290b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "b365c0b4-06e3-4fef-a555-16fd0877dc7c"
  override val correlationId = "b5fht738957jfjf845jgjf855"
}

case object FeedbackThree extends CalculationIdDetails {
  override val calculationId = "333390b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "c365c0b4-06e3-4fef-a555-16fd0877dc7c"
  override val correlationId = "c5fht738957jfjf845jgjf855"
}

case object FeedbackFour extends CalculationIdDetails {
  override val calculationId = "444490b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "d365c0b4-06e3-4fef-a555-16fd0877dc7c"
  override val correlationId = "d5fht738957jfjf845jgjf855"
}

case object FeedbackFive extends CalculationIdDetails {
  override val calculationId = "555590b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "e365c0b4-06e3-4fef-a555-16fd0877dc7c"
  override val correlationId = "efht738957jfjf845jgjf855"
}

case object FeedbackForDefaultResponse extends CalculationIdDetails {
  override val calculationId = "000090b4-06e3-4fef-a555-6fd0877dc7de"
  override val feedbackId = "a365c0b4-06e3-4fef-a555-16fd0877dc7c"
  override val correlationId = "g5fht738957jfjf845jgjf855"
}

case object FeedbackInvalidCalculationId extends CalculationIdDetails {
  override val calculationId = "999990b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "e365c0b4-06e3-4fef-a555-16fd0877dc7c"
  override val correlationId = "efht738957jfjf845jgjf855"
}

case object FeedbackMissingCalculationId extends CalculationIdDetails {
  override val calculationId = "101090b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "a365c0b4-06e3-4fef-a555-16fd0877dc7c"
  override val correlationId = "mfht738957jfjf845jgjf855"
}

case object FeedbackFromRDS extends CalculationIdDetails {
  override val calculationId = "666690b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackId = "a365c0b4-06e3-4fef-a555-16fd0877dc7c"
  override val correlationId = "mfht738957jfjf845jgjf855"
}
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
  val calculationID: String
  val feedbackID: String
  val correlationID: String
}

case object FeedbackOne extends CalculationIdDetails {
  override val calculationID = "111190b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackID = "a365c0b4-06e3-4fef-a555-16fd0877dc7c"
  override val correlationID = "a5fht738957jfjf845jgjf855"
}

case object FeedbackTwo extends CalculationIdDetails {
  override val calculationID = "222290b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackID = "b365c0b4-06e3-4fef-a555-16fd0877dc7c"
  override val correlationID = "b5fht738957jfjf845jgjf855"
}

case object FeedbackThree extends CalculationIdDetails {
  override val calculationID = "333390b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackID = "c365c0b4-06e3-4fef-a555-16fd0877dc7c"
  override val correlationID = "c5fht738957jfjf845jgjf855"
}

case object FeedbackFour extends CalculationIdDetails {
  override val calculationID = "444490b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackID = "d365c0b4-06e3-4fef-a555-16fd0877dc7c"
  override val correlationID = "d5fht738957jfjf845jgjf855"
}

case object FeedbackFive extends CalculationIdDetails {
  override val calculationID = "555590b4-06e3-4fef-a555-6fd0877dc7ca"
  override val feedbackID = "e365c0b4-06e3-4fef-a555-16fd0877dc7c"
  override val correlationID = "efht738957jfjf845jgjf855"
}

case object FeedbackForDefaultResponse extends CalculationIdDetails {
  override val calculationID = "999490b4-06e3-4fef-a555-6fd0877dc7de"
  override val feedbackID = "g365c0b4-06e3-4fef-a555-6fd0877dc7c"
  override val correlationID = "g5fht738957jfjf845jgjf855"
}
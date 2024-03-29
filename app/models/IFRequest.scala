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

package models

import play.api.libs.json._

import java.time.OffsetDateTime

case class IFRequest(
                      serviceRegime: String,
                      eventName: String,
                      eventTimestamp: OffsetDateTime,
                      feedbackId: String,
                      metaData: List[Map[String, String]],
                      payload: Option[Messages]
                    )

case class Messages(messages: Option[Seq[IFRequestPayload]])

object IFRequest {
  implicit val messageFormats: Format[Messages] = Json.format[Messages]
  implicit val formats: Format[IFRequest] = Json.format[IFRequest]
}


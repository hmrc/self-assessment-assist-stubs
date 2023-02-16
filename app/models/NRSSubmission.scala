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

import play.api.libs.json._

import java.time.LocalDateTime

case class NRSSubmission(payload: String, metadata: MetaData)

object NRSSubmission {
  implicit val formats: OFormat[NRSSubmission] = Json.format[NRSSubmission]
}


case class MetaData(businessId: String, notableEvent: String, payloadContentType: String, payloadSha256Checksum: String, userSubmissionTimestamp: LocalDateTime, identityData: JsObject, userAuthToken: String, headerData: JsObject, searchKeys: SearchKeys)

object MetaData {
  implicit val formats: OFormat[MetaData] = Json.format[MetaData]
}


case class SearchKeys(searchKey: String)

object SearchKeys {
  implicit val reads: Reads[SearchKeys] =
    (__ \ "reportId").read[String]
      .map(SearchKeys(_))

  implicit val writes: OWrites[SearchKeys] = Json.writes[SearchKeys]
}

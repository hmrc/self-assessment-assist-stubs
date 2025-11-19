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

package common

import config.AppConfig
import models._
import play.api.Logging
import play.api.libs.json.{JsValue, Json}

import java.io.File
import javax.inject.{Inject, Singleton}
import scala.io.Source.fromFile
import scala.util.Using

@Singleton
class StubResource @Inject()(appConfig: AppConfig) extends Logging {

  val isSandboxMode: Boolean = appConfig.disableErrorResponses

  private val explicitResponseCalcIds: Set[String] = Set(
    FeedbackHttp201ResponseCode204.calculationId,
    FeedbackHttp201ResponseCode404.calculationId,
    RdsInvalidRespWithMissingCalculationId.calculationId
  )

  def loadSubmitResponseTemplate(calculationId: String, feedbackId: String, correlationId: String): JsValue = {
    val shouldConvertErrorToSuccess: Boolean = calculationId == RdsInvalidRespWithMissingCalculationId.calculationId && isSandboxMode

    val fileName: String = if (!shouldConvertErrorToSuccess && explicitResponseCalcIds.contains(calculationId)) {
      logger.info(s"[StubResource][loadSubmitResponseTemplate] Loading explicit response file for $calculationId")
      s"conf/response/submit/$calculationId-response.json"
    } else {
      logger.info(s"[StubResource][loadSubmitResponseTemplate] Loading default success file for $calculationId")
      "conf/response/submit/default-success-response.json"
    }

    val templateContent: String = findResource(fileName).getOrElse(
      throw new IllegalStateException(s"[StubResource][loadSubmitResponseTemplate] Submit template parsing failed: $fileName")
    )

    Json.parse(
      templateContent
        .replace("replaceFeedbackId", feedbackId)
        .replace("replaceCalculationId", calculationId)
        .replace("replaceCorrelationId", correlationId)
    )
  }

  def loadAckResponseTemplate(feedbackId: String, nino: String, fileName: String): JsValue = {
    val templateContent: String = findResource(fileName).getOrElse(
      throw new IllegalStateException(s"[StubResource][loadAckResponseTemplate] Acknowledge template parsing failed: $fileName")
    )

    Json.parse(
      templateContent
        .replace("replaceFeedbackId", feedbackId)
        .replace("replaceNino", nino)
    )
  }

  def findResource(path: String): Option[String] = {
    val file: File = new File(path)

    if (file.exists()) {
      Using(fromFile(file))(_.mkString).toOption
    } else {
      logger.error(s"[StubResource][findResource] File not found: $path")
      None
    }
  }
}

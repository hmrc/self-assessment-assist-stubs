/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package utils

import org.apache.commons.codec.binary.Base64
import play.api.libs.json._

import scala.io.Source

object JsonUtils {

  def jsonFromFile(path: String): JsValue = {
    val resource = Source.fromURL(getClass.getResource(path))
    val json = Json.parse(resource.mkString)
    resource.close()
    json
  }

  def base64JsonFromFile(path: String): String = {
    val submissionPayload = jsonFromFile(path)
    Base64.encodeBase64URLSafeString(Json.toBytes(submissionPayload))
  }
}

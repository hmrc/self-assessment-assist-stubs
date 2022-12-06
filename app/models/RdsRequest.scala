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

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json._
import models.RdsRequest.Input
import uk.gov.hmrc.http.BadRequestException

import java.util.UUID

case class RdsRequest(inputs: Seq[Input]) {

  def calculationId: UUID =
    inputs.find(_.name == "calculationId").map(_.value.toString).map(UUID.fromString)
      .getOrElse(throw new BadRequestException("No 'calculationId' present."))

  def ninoValue: String =
    inputs.find(_.name == "nino").map(_.value.toString)
      .getOrElse(throw new BadRequestException("No 'nino' present."))//TODO fix me later, not the right exception

  def feedbackId: String =
    inputs.find(_.name == "feedbackId").map(_.value.toString)
      .getOrElse(throw new BadRequestException("No 'feedbackId' present."))

  def correlationId: String =
    inputs.find(_.name == "correlationId").map(_.value.toString)
      .getOrElse(throw new BadRequestException("No 'correlationId' present."))

  def fraudRiskReportReasons: Seq[Any] =
    inputs.find(_.name == "fraudRiskReportReasons").map(r => Seq(r.value))
      .getOrElse(throw new BadRequestException("The value of the field \"fraudRiskReportReasons\" must not be empty or missing."))

}

object RdsRequest {

  trait Input {
    def name: String

    def value: Any
  }

  trait ObjectPart

  case class InputWithString(name: String, value: String) extends Input

  case class InputWithInt(name: String, value: Int) extends Input

  case class InputWithObject(name: String, value: Seq[ObjectPart]) extends Input

  case class InputWithBoolean(name: String, value: Boolean) extends Input

  case class MetadataWrapper(metadata: Seq[Map[String, String]]) extends ObjectPart

  case class DataWrapper(data: Seq[Seq[String]]) extends ObjectPart

  object Input {
    implicit val reads: Reads[Input] = {
      case json@JsObject(values) =>
        values.get("value") match {
          case Some(JsString(_)) => InputWithString.reads.reads(json)
          case Some(JsNull) => InputWithString.reads.reads(json)
          case Some(JsNumber(_)) => InputWithInt.reads.reads(json)
          case Some(JsArray(_)) => InputWithObject.reads.reads(json)
          case Some(JsBoolean(_)) => InputWithBoolean.reads.reads(json)
        }
    }

    implicit val writes: Writes[Input] = {
      case i@InputWithString(_, _) => InputWithString.writes.writes(i)
      case i@InputWithInt(_, _) => InputWithInt.writes.writes(i)
      case i@InputWithObject(_, _) => InputWithObject.writes.writes(i)
    }

  }

  object InputWithString {

    val reads: Reads[InputWithString] =
      (JsPath \ "name").read[String]
        .and((JsPath \ "value").readWithDefault[String](null))(InputWithString.apply _)

    val writes: Writes[InputWithString] =
      (JsPath \ "name").write[String]
        .and((JsPath \ "value").write[String])(unlift(InputWithString.unapply))

  }

  object InputWithInt {

    val reads: Reads[InputWithInt] =
      (JsPath \ "name").read[String]
        .and((JsPath \ "value").read[Int])(InputWithInt.apply _)

    val writes: Writes[InputWithInt] =
      (JsPath \ "name").write[String]
        .and((JsPath \ "value").write[Int])(unlift(InputWithInt.unapply))

  }

  object InputWithObject {

    val reads: Reads[InputWithObject] =
      (JsPath \ "name").read[String]
        .and((JsPath \ "value").read[Seq[ObjectPart]])(InputWithObject.apply _)

    val writes: Writes[InputWithObject] =
      (JsPath \ "name").write[String]
        .and((JsPath \ "value").write[Seq[ObjectPart]])(unlift(InputWithObject.unapply))

  }

  object InputWithBoolean {

    val reads: Reads[InputWithBoolean] =
      (JsPath \ "name").read[String]
        .and((JsPath \ "value").readWithDefault[Boolean](false))(InputWithBoolean.apply _)

    val writes: Writes[InputWithBoolean] =
      (JsPath \ "name").write[String]
        .and((JsPath \ "value").write[Boolean])(unlift(InputWithBoolean.unapply))

  }

/*  object InputWithArray {

    val reads: Reads[InputWithArray] =
      (JsPath \ "name").read[String]
        .and((JsPath \ "value").readWithDefault[Boolean](false))(InputWithBoolean.apply _)

    val writes: Writes[InputWithArray] =
      (JsPath \ "name").write[String]
        .and((JsPath \ "value").write[Boolean])(unlift(InputWithBoolean.unapply))

  }*/
  object ObjectPart {

    implicit val reads: Reads[ObjectPart] = {
      case json@JsObject(values) =>
        values.keys.toList match {
          case List("metadata") => MetadataWrapper.reads.reads(json)
          case List("data") => DataWrapper.reads.reads(json)
        }
    }

    implicit val writes: Writes[ObjectPart] = {
      case o@MetadataWrapper(_) => MetadataWrapper.writes.writes(o)
      case o@DataWrapper(_) => DataWrapper.writes.writes(o)
    }

  }

  object MetadataWrapper {

    val reads: Reads[MetadataWrapper] =
      (JsPath \ "metadata").read[Seq[Map[String, String]]].map(MetadataWrapper.apply)

    val writes: Writes[MetadataWrapper] =
      (JsPath \ "metadata").write[Seq[Map[String, String]]].contramap(_.metadata)

  }

  object DataWrapper {

    val reads: Reads[DataWrapper] =
      (JsPath \ "data").read[Seq[Seq[String]]].map(DataWrapper.apply)

    val writes: Writes[DataWrapper] =
      (JsPath \ "data").write[Seq[Seq[String]]].contramap(_.data)

  }

  implicit val reads: Reads[RdsRequest] =
    (JsPath \ "inputs").read[Seq[Input]].map(RdsRequest.apply)

  implicit val writes: Writes[RdsRequest] =
    (JsPath \ "inputs").write[Seq[Input]].contramap(_.inputs)

}

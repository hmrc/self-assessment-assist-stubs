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

package utils

import play.api.libs.json.{JsValue, Json}

import java.io.{File, FileNotFoundException}
import scala.collection.immutable.HashMap
import scala.io.Source
import scala.util.{Failure, Success, Try}


case class LookupValue(nino: String, stausCode: Int, jsonReturn: JsValue)

case class JsonStore(fileName: String, fileterKey: String) {
  val hashMap: Option[HashMap[String, LookupValue]] =
    JsonStore.LoadData(fileName, fileterKey)

  def get(key: String): Option[LookupValue] =
    JsonStore.get(key, hashMap)
}

case class KeyValue(key: String, value: LookupValue)

object JsonStore {

  def LoadData(fileName: String, filterKey: String): Option[HashMap[String, LookupValue]] = {

    val fileContentsOpt = GetFileContents(fileName)
    val ret = ParseToHashMap(fileContentsOpt, filterKey)

    ret
  }

  def get(key: String, hashMap: Option[HashMap[String, LookupValue]]): Option[LookupValue] = {
    hashMap match {
      case None => None
      case Some(hm) => hm.get(key).orElse(None)
    }


  }


  def GetFileContents(fileName:String): Option[String] = {
    Try {
      val file = new File(fileName)
      val absolutePath = file.getAbsolutePath

      val fromFileOption = Option(Source.fromFile(absolutePath))
      val value: String = fromFileOption.get.getLines.mkString
      ( fromFileOption, value)
    } match {
      case Success( (fromFileOption, value) ) =>
        try {
          fromFileOption.get.close
        } catch {
          case _:Throwable =>
            Error(s"Unable to close file ${fileName}")
            throw new ExceptionInInitializerError
        }
        Some(value)

      case Failure(ex) =>
        ex match {
          case ex: FileNotFoundException =>
            Error(s"Unable to open file ${fileName}")
            None
          case _ =>
            Error(s"Unable to initialise store from file ${fileName}")
            throw new ExceptionInInitializerError
        }
    }
  }


  private def ParseToHashMap(fileContentsOpt: Option[String], filterKey:String ): Option[HashMap[String, LookupValue]] = {
    fileContentsOpt match {
      case None => Option(new HashMap[String, LookupValue])
      case Some(s: String) =>
        val jsonTry = Try {
          val json: JsValue = Json.parse(s)
          json
        }

        jsonTry match {
          case Success(v) =>
            val keyCommands = v \\ "key"

            val keyValue: Seq[KeyValue] = for {
              keyCommand <- keyCommands
              key:String =  (keyCommand \ "nino").as[String]
              stage:String =  (keyCommand \ "stage").as[String]
              if (stage == filterKey)
              statusCode:Int = (keyCommand \ "statuscode").as[Int]
              jsReturn:JsValue = ( keyCommand \ "return").get
              lookupValue = LookupValue(key, statusCode, jsReturn)
              x = KeyValue(key, lookupValue)
            } yield x

            val r: HashMap[String, LookupValue] = HashMap(keyValue.map(x => x.key -> x.value): _*)
            if (r.size!=keyValue.length) {
              val duplicateNinos = keyValue.map(_.key).toList.diff(r.map(_._1).toSet.toList)
              Error(s"Duplicate key values ${duplicateNinos}")
            }

            Some(r)
          case Failure(ex) => None
        }
    }
  }

  private def Error(error:String):Unit = {

  }

}
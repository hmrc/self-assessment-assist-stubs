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

package controllers.actions

import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait HeaderValidator {

  val CONTENT_TYPE_HEADER = "Content-Type"
  val API_KEY_HEADER = "X-API-Key"
  val TOKEN_HEADER = "Authorization"
  private val VALID_CONTENT_TYPE = "application/json"
  private val VALID_API_KEY = "dummy-api-key"
  private val VALID_TOKEN_VALUE = "dummy-api-key"

  def isApiKeyValid(request: Request[_]): Boolean = {
    val tokenValue = request.headers.get(API_KEY_HEADER).getOrElse("Invalid")
    tokenValue.contains(VALID_API_KEY)
  }

  def isAuthTokenValid(request: Request[_]): Boolean = {
    val tokenValue = request.headers.get(TOKEN_HEADER).getOrElse("Invalid")
    tokenValue.contains(VALID_TOKEN_VALUE)
  }

  def isContentTypeValid(request: Request[_]): Boolean = {
    val tokenValue = request.headers.get(CONTENT_TYPE_HEADER).getOrElse("Invalid")
    tokenValue.contains(VALID_CONTENT_TYPE)
  }
}

class HeaderValidatorAction @Inject()(parser: BodyParsers.Default)
                                     (implicit val ec: ExecutionContext) extends ActionBuilderImpl(parser) with HeaderValidator {

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {

    val (isValidAuth, requestValid) = (isAuthTokenValid(request), isContentTypeValid(request))

    if (isValidAuth && requestValid) {
      block(request)
    } else {

      Future.successful(Results.Unauthorized)
    }
  }
}

class NrsHeaderValidatorAction @Inject()(parser: BodyParsers.Default)
                                     (implicit val ec: ExecutionContext) extends ActionBuilderImpl(parser) with HeaderValidator {

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {

    val (apiKeyValid, requestValid) = (isApiKeyValid(request), isContentTypeValid(request))

    if (apiKeyValid && requestValid) {
      block(request)
    } else {
      Future.successful(Results.Unauthorized)
    }
  }
}

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

package controllers

import base.SpecBase
import config.AppConfig
import controllers.actions.HeaderValidatorAction
import models._
import org.apache.pekko.stream.testkit.NoMaterializer
import play.api.Configuration
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{BodyParsers, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import java.time.OffsetDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IfsControllerSpec extends SpecBase {

  private def makeAppConfig(disableErrorResponses: Boolean): AppConfig =
    new AppConfig(Configuration.from(Map("feature-switch.disable-error-responses" -> disableErrorResponses)))

  private class Test(disableErrorResponses: Boolean = false) {
    private val headerValidatorAction: HeaderValidatorAction = new HeaderValidatorAction(new BodyParsers.Default()(NoMaterializer))
    private val appConfig: AppConfig = makeAppConfig(disableErrorResponses)

    val controller: IfsController = new IfsController(headerValidatorAction, stubControllerComponents(), appConfig)
  }

  private val invalidLinks: Seq[IFRequestPayloadActionLinks] = Seq(
    IFRequestPayloadActionLinks("[title", "url"),
    IFRequestPayloadActionLinks("title]", "url"),
    IFRequestPayloadActionLinks("title", "[url"),
    IFRequestPayloadActionLinks("title", "url]")
  )

  private def generateRequest(calculationId: String,
                              links: Option[Seq[IFRequestPayloadActionLinks]]): IFRequest = IFRequest(
    serviceRegime = "self-assessment-assist",
    eventName = "GenerateReport",
    eventTimestamp = OffsetDateTime.now(),
    feedbackId = "feedbackId",
    metaData = List(
      Map("nino"                 -> "nino"),
      Map("taxYear"              -> "2023"),
      Map("calculationId"        -> calculationId),
      Map("customerType"         -> "Agent"),
      Map("agentReferenceNumber" -> "12345"),
      Map("calculationTimestamp" -> "2019-02-15T09:35:15.094Z")
    ),
    payload = Some(
      Messages(
        Some(
          List(
            IFRequestPayload(
              messageId = "messageId",
              englishAction = IFRequestPayloadAction(
                title = "title",
                message = "message",
                action = "action",
                path = "path",
                links = links
              ),
              welshAction = IFRequestPayloadAction(
                title = "TODO - title",
                message = "TODO - message",
                action = "TODO - action",
                path = "TODO - path",
                links = links
              )
            )
          )
        )
      )
    )
  )

  private def acknowledgeRequest(feedbackId: String): IFRequest = IFRequest(
    serviceRegime = "self-assessment-assist",
    eventName = "AcknowledgeReport",
    eventTimestamp = OffsetDateTime.now(),
    feedbackId = feedbackId,
    metaData = List(
      Map("nino"    -> "nino"),
      Map("taxYear" -> "2023")
    ),
    payload = Some(
      Messages(
        Some(
          List(
            IFRequestPayload(
              messageId = "messageId",
              englishAction = IFRequestPayloadAction(
                title = "title",
                message = "message",
                action = "action",
                path = "path",
                links = None
              ),
              welshAction = IFRequestPayloadAction(
                title = "TODO - title",
                message = "TODO - message",
                action = "TODO - action",
                path = "TODO - path",
                links = None
              )
            )
          )
        )
      )
    )
  )

  private def callSubmit(controller: IfsController, requestBody: JsValue): Future[Result] = {
    val request: FakeRequest[JsValue] = FakeRequest("POST", "/interaction-data/store-interactions")
      .withBody(requestBody)
      .withHeaders(("Authorization", "ABCD1234"), ("Content-Type", "application/json"))

    controller.submit().apply(request)
  }

  private def testSubmitStoreInteraction(controller: IfsController,
                                         calculationId: String,
                                         expectedStatus: Int,
                                         expectedBody: Option[JsValue],
                                         links: Option[Seq[IFRequestPayloadActionLinks]] = None): Unit = {
    val requestBody: JsValue = Json.toJson(generateRequest(calculationId, links))

    val result: Future[Result] = callSubmit(controller, requestBody)

    status(result) mustBe expectedStatus
    expectedBody match {
      case Some(expectedJson) => contentAsJson(result) mustBe expectedJson
      case None               => contentAsString(result) mustBe empty
    }
  }

  private def testSubmitStoreAcknowledgement(controller: IfsController,
                                             feedbackId: String,
                                             expectedStatus: Int,
                                             expectedBody: Option[JsValue]): Unit = {
    val requestBody: JsValue = Json.toJson(acknowledgeRequest(feedbackId))

    val result: Future[Result] = callSubmit(controller, requestBody)

    status(result) mustBe expectedStatus
    expectedBody match {
      case Some(expectedJson) => contentAsJson(result) mustBe expectedJson
      case None               => contentAsString(result) mustBe empty
    }
  }

  private val featureSwitchTestCases: Seq[(Boolean, String)] = Seq((true, "enabled"), (false, "disabled"))

  "IfsController" when {
    "submit" when {
      featureSwitchTestCases.foreach { case (disableErrorResponses, scenario) =>
        s"processing a generate report event and feature switch is $scenario" must {
          "return 204 NO_CONTENT when a valid calculationId is supplied" in new Test(disableErrorResponses) {
            testSubmitStoreInteraction(controller, "good-one", NO_CONTENT, None)
          }

          "return 400 BAD_REQUEST" when {
            "a request with invalid links is supplied" in new Test(disableErrorResponses) {
              val expectedResponse: JsValue = controller.invalidPayloadError

              invalidLinks.foreach { link =>
                testSubmitStoreInteraction(controller, "good-one", BAD_REQUEST, Some(expectedResponse), Some(Seq(link)))
              }
            }

            "an invalid request without calculationId is supplied" in new Test(disableErrorResponses) {
              val expectedResponse: JsValue = controller.invalidPayloadError

              val requestBody: JsValue = Json.toJson(generateRequest("good-one", None).copy(metaData = Nil))

              val result: Future[Result] = callSubmit(controller, requestBody)

              status(result) mustBe BAD_REQUEST
              contentAsJson(result) mustBe expectedResponse
            }
          }

          "handle all IFS errors" in new Test(disableErrorResponses) {
            val errorCases: Seq[(Int, String, Option[JsValue])] = Seq(
              (BAD_REQUEST, IfsServiceBadRequest400.calculationId, Some(controller.invalidCorrelationIdError)),
              (REQUEST_TIMEOUT, IfsServiceRequestTimeout408.calculationId, None),
              (INTERNAL_SERVER_ERROR, IfsInternalServerError500.calculationId, Some(controller.internalServerError)),
              (SERVICE_UNAVAILABLE, IfsServiceNotAvailable503.calculationId, Some(controller.serviceUnavailableError))
            )

            errorCases.foreach { case (statusCode, calculationId, expectedBody) =>
              val (code, body): (Int, Option[JsValue]) = if (disableErrorResponses) {
                (NO_CONTENT, None)
              } else {
                (statusCode, expectedBody)
              }

              testSubmitStoreInteraction(controller, calculationId, code, body)
            }
          }
        }
      }

      "processing an acknowledge report event" must {
        "return 204 NO_CONTENT when a valid feedbackId is supplied" in new Test() {
          testSubmitStoreAcknowledgement(controller, "good-one", NO_CONTENT, None)
        }

        "handle all IFS errors" in new Test() {
          val errorCases: Seq[(Int, String, Option[JsValue])] = Seq(
            (BAD_REQUEST, IfsServiceBadRequest400.feedbackId, Some(controller.invalidPayloadError)),
            (REQUEST_TIMEOUT, IfsServiceRequestTimeout408.feedbackId, None),
            (INTERNAL_SERVER_ERROR, IfsInternalServerError500.feedbackId, Some(controller.internalServerError)),
            (SERVICE_UNAVAILABLE, IfsServiceNotAvailable503.feedbackId, Some(controller.serviceUnavailableError))
          )

          errorCases.foreach { case (statusCode, feedbackId, expectedBody) =>
            testSubmitStoreAcknowledgement(controller, feedbackId, statusCode, expectedBody)
          }
        }
      }

      "an invalid request is supplied must return 400 BAD_REQUEST" in new Test() {
        val expectedResponse: JsValue = controller.invalidPayloadError

        val result: Future[Result] = callSubmit(controller, JsObject.empty)

        status(result) mustBe BAD_REQUEST
        contentAsJson(result) mustBe expectedResponse
      }
    }
  }
}

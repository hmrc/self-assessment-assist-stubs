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

package controllers

import base.SpecBase
import models.{FeedbackFiveHttp201ResponseCode201, FeedbackFourHttp201ResponseCode201, IFRequest, IFRequestPayload, IFRequestPayloadAction, Messages}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._

import java.time.OffsetDateTime
import scala.concurrent.Future

class IfsControllerSpec extends SpecBase{

  private val controller: IfsController = app.injector.instanceOf[IfsController]

  private def submitStoreInteraction(calculationId: String): Future[Result] = {
    val req = IFRequest(
      serviceRegime = "self-assessment-assist",
      eventName = "GenerateReport",
      eventTimestamp = OffsetDateTime.now(),
      feedbackId = "feedbackId",
      metadata = List(
        Map("nino" -> "nino"),
        Map("taxYear" -> "2023"),
        Map("calculationId" -> calculationId),
        Map("customerType" -> "Agent"),
        Map("agentReferenceNumber" -> "12345"),
        Map("calculationTimestamp" -> "2019-02-15T09:35:15.094Z")
      ),
      payload = Some(Messages(Some(Seq(IFRequestPayload(
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
        ))))
      )))

    val fakeRequest: FakeRequest[JsValue] = FakeRequest("POST", "/interaction-data/store-interactions").withBody(Json.toJson(req)).withHeaders(("X-API-Key", "dummy-api-key"),("Content-Type", "application/json") )
    controller.submit().apply(fakeRequest)
  }

  private def submitStoreAcknowledgement(feedbackId: String): Future[Result] = {
    val req = IFRequest(
      serviceRegime = "self-assessment-assist",
      eventName = "AcknowledgeReport",
      eventTimestamp = OffsetDateTime.now(),
      feedbackId,
      metadata = List(
        Map("nino" -> "nino"),
        Map("taxYear" -> "2023"),
      ),
      payload = Some(Messages(Some(List(IFRequestPayload(
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
        ))))
      )))

    val fakeRequest: FakeRequest[JsValue] = FakeRequest("POST", "/interaction-data/store-interactions").withBody(Json.toJson(req)).withHeaders(("X-API-Key", "dummy-api-key"),("Content-Type", "application/json") )
    controller.submit().apply(fakeRequest)
  }

  "IfsController.submit()" when {

    "generate report: provided with a calculation id in metadata" must {
      "return 204" in {
        val result = submitStoreInteraction("good one")
        status(result) must be(NO_CONTENT)
      }
    }

    "generate report: provided with a calculation id in metadata to trigger invalid correlationId" must {
      "return 400" in {
        val result = submitStoreInteraction(FeedbackFiveHttp201ResponseCode201.calculationId)
        status(result) must be(BAD_REQUEST)
      }
    }

    "generate report: provided with a calculation id in metadata to service unavailable" must {
      "return 503" in {
        val result = submitStoreInteraction(FeedbackFourHttp201ResponseCode201.calculationId)
        status(result) must be(SERVICE_UNAVAILABLE)
      }
    }

    "acknowledge report: provided with a calculation id in metadata for no content" must {
      "return 204" in {
        val result = submitStoreAcknowledgement("feedbackId")
        status(result) must be(NO_CONTENT)
      }
    }

    "acknowledge report: provided with a feedback id trigger invalid bad request" must {
      "return 400" in {
        val result = submitStoreAcknowledgement(FeedbackFiveHttp201ResponseCode201.feedbackId)
        status(result) must be(BAD_REQUEST)
      }
    }

    "acknowledge report: provided with a feedback id trigger invalid service unavailable" must {
      "return 503" in {
        val result = submitStoreAcknowledgement(FeedbackFourHttp201ResponseCode201.feedbackId)
        status(result) must be(SERVICE_UNAVAILABLE)
      }
    }
  }
}

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
import models.{IFRequest, IFRequestPayload, IFRequestPayloadAction, IfsServiceBadRequest400, IfsServiceInternalServiceError500, IfsServiceNotAvailable503, IfsServiceRequestTimeout408, Messages}
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

    val fakeRequest: FakeRequest[JsValue] = FakeRequest("POST", "/interaction-data/store-interactions").withBody(Json.toJson(req)).withHeaders(("Authorization", "ABCD1234"),("Content-Type", "application/json") )
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

    val fakeRequest: FakeRequest[JsValue] = FakeRequest("POST", "/interaction-data/store-interactions").withBody(Json.toJson(req)).withHeaders(("Authorization", "ABCD1234"),("Content-Type", "application/json") )
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
        val result = submitStoreInteraction(IfsServiceBadRequest400.calculationId)
        status(result) must be(BAD_REQUEST)
      }
    }

    "generate report: provided with a calculation id in metadata to service unavailable" must {
      "return 503" in {
        val result = submitStoreInteraction(IfsServiceNotAvailable503.calculationId)
        status(result) must be(SERVICE_UNAVAILABLE)
      }
    }

    "generate report: provided with a calculation id in metadata to trigger request timeout" must {
      "return 408" in {
        val result = submitStoreInteraction(IfsServiceRequestTimeout408.calculationId)
        status(result) must be(REQUEST_TIMEOUT)
      }
    }

    "generate report: with a calculation id in metadata to trigger internal server error" must {
      "return 500" in {
        val result = submitStoreInteraction(IfsServiceInternalServiceError500.calculationId)
        status(result) must be(INTERNAL_SERVER_ERROR)
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
        val result = submitStoreAcknowledgement(IfsServiceBadRequest400.feedbackId)
        status(result) must be(BAD_REQUEST)
      }
    }

    "acknowledge report: provided with a feedback id trigger request timeout" must {
      "return 408" in {
        val result = submitStoreAcknowledgement(IfsServiceRequestTimeout408.feedbackId)
        status(result) must be(REQUEST_TIMEOUT)
      }
    }

    "acknowledge report: provided with a feedback id trigger internal server error" must {
      "return 500" in {
        val result = submitStoreAcknowledgement(IfsServiceInternalServiceError500.feedbackId)
        status(result) must be(INTERNAL_SERVER_ERROR)
      }
    }

    "acknowledge report: provided with a feedback id trigger invalid service unavailable" must {
      "return 503" in {
        val result = submitStoreAcknowledgement(IfsServiceNotAvailable503.feedbackId)
        status(result) must be(SERVICE_UNAVAILABLE)
      }
    }

  }
}

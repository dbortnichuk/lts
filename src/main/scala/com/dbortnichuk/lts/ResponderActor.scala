package com.dbortnichuk.lts

import akka.actor.{Actor, ActorLogging, PoisonPill}
import com.dbortnichuk.lts.Protocol.{ProcessedMessage, RequestsLimitExceeded, SystemResponse}
import spray.http.StatusCodes
import spray.routing.RequestContext
import spray.httpx.SprayJsonSupport._

class ResponderActor(requestContext: RequestContext) extends Actor with ActorLogging {

  def receive = {

    case processed: ProcessedMessage =>
      requestContext.complete(StatusCodes.OK, SystemResponse(processed))
      selfTerminate

    case RequestsLimitExceeded =>
      requestContext.complete(StatusCodes.TooManyRequests)
      selfTerminate
  }

  def selfTerminate = self ! PoisonPill

}

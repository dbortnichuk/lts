package com.dbortnichuk.lts

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.dbortnichuk.lts.Protocol.{ProcessedMessage, UserRequest}
import com.typesafe.config.Config
import spray.routing.RequestContext


case class Process(userRequest: UserRequest, requestContext: RequestContext)

case class ProcessingAllowed(userRequest: UserRequest, responder: ActorRef)

class ProcessorActor(config: Config) extends Actor with ActorLogging {

  private val graceRps = config.getInt("throttling.grace-rps")
  private val throttlingEnabled = config.getBoolean("throttling.enabled")

  private val throttlingService = context.actorOf(Props(new ThrottlingServiceActor(graceRps)))

  def receive = {
    case Process(userReq, requestContext) if throttlingEnabled => {
      throttlingService ! Throttle(userReq, createResponder(requestContext))
    }

    case Process(userReq, requestContext) => {
      self ! ProcessingAllowed(userReq, createResponder(requestContext))
    }

    case ProcessingAllowed(userReq, responder) => {
      //log.info(userReq.toString) // do processing here
      responder ! ProcessedMessage(userReq.payload) //echo payload
    }

  }

  private def createResponder(requestContext: RequestContext): ActorRef = {
    context.actorOf(Props(new ResponderActor(requestContext)))
  }

}

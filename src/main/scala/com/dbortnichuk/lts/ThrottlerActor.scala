package com.dbortnichuk.lts

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.dbortnichuk.lts.Protocol.{RequestsLimitExceeded, UserRequest}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

case class Throttle(userRequest: UserRequest, responder: ActorRef)

case object NextTimeSlot

/**
  *
  * @param timeSlotDuration duration of a single time slot throttling is being accounted in sec
  * @param rpdLimit         request per duration max limit in sec
  */

class ThrottlerActor(timeSlotDuration: Int, rpdLimit: Int) extends Actor with ActorLogging {

  private implicit val executionContext: ExecutionContext = context.dispatcher
  context.system.scheduler.schedule((timeSlotDuration * 2).second, timeSlotDuration.second, self, NextTimeSlot)

  private var messagesCurrentTimeSlot: Int = 0

  def receive = {
    case Throttle(userReq, responder) if messagesCurrentTimeSlot >= rpdLimit =>
      responder ! RequestsLimitExceeded
      messagesCurrentTimeSlot += 1
    case Throttle(userReq, responder) =>
      sender ! ProcessingAllowed(userReq, responder)
      messagesCurrentTimeSlot += 1
    case NextTimeSlot =>
      messagesCurrentTimeSlot = 0
  }

}

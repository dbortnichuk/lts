package com.dbortnichuk.lts

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.dbortnichuk.lts.Protocol.{RequestsLimitExceeded, Sla, UserRequest}
import akka.dispatch._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, TimeoutException}

case object ClearCaches

class ThrottlingServiceActor(graceRps: Int) extends Actor with ActorLogging {

  private implicit val executionContext: ExecutionContext = context.dispatcher
  context.system.scheduler.schedule(20.minute, 10.minute, self, ClearCaches)

  private val slaService: SlaService = new SlaServiceImpl

  private val throttlingCache = scala.collection.mutable.HashMap.empty[Sla, ActorRef]
  private val slaCache = scala.collection.mutable.HashMap.empty[String, Sla]

  private val TokenUnauthorized = "unauthorized"
  private val unauthorizedSla = Sla(TokenUnauthorized, graceRps)

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    slaCache.put(TokenUnauthorized, unauthorizedSla)
    throttlingCache.put(unauthorizedSla, createRPSThrottler(graceRps))
  }

  def receive = {
    case throttleMessage: Throttle =>
      val token: String = throttleMessage.userRequest.token.getOrElse(TokenUnauthorized)
      val sla: Sla = slaCache.get(token).getOrElse {
        try {
          val slaFromService: Sla = Await.result(slaService.getSlaByToken(token), 300.millis)
          slaCache.put(token, slaFromService)
          slaFromService
        } catch {
          case te: TimeoutException => {
            log.info("Sla service timeout, treating as unauthorized")
            unauthorizedSla
          }
          case e: Exception => {
            log.info("no corresponding user found, treating as unauthorized")
            unauthorizedSla
          }
        }
      }
      val throttler = throttlingCache.get(sla).getOrElse {
        val thrtlr = createRPSThrottler(sla.rps)
        throttlingCache.put(sla, thrtlr)
        thrtlr
      }

      throttler ! throttleMessage

    case procAllowedMsg: ProcessingAllowed =>
      context.parent ! procAllowedMsg

    case ClearCaches => {
      throttlingCache.clear()
      slaCache.clear()
    }

  }

  private def createRPSThrottler(rps: Int): ActorRef = {
    createThrottler(1, rps)
  }

  private def createThrottler(timeSlotDuration: Int, rpdLimit: Int): ActorRef = {
    context.actorOf(Props(new ThrottlerActor(timeSlotDuration, rpdLimit)))
  }

}

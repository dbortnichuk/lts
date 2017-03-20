package com.dbortnichuk.lts.test

import akka.actor.{Actor, ActorLogging, PoisonPill}
import play.api.libs.ws.WS
import spray.http.StatusCodes

import scala.collection.mutable.ArrayBuffer


case class Generate(results: ArrayBuffer[Int])

class TestActor(initialTicks: Int, rpi: Int) extends Actor with RestClient {

  import com.dbortnichuk.lts.SlaService._

  private var ticksLeft = initialTicks

  def receive = {
    case Generate(results) => {
      if (ticksLeft >= 0) {
        val res = (1 to rpi).map(idx => if (idx % 2 == 0) TokenJCVD else TokenHans).map(createRequest(_))
        results.appendAll(res)
        ticksLeft -= 1
      } else {
        self ! PoisonPill
        context.system.shutdown()
      }
    }
  }

  def createRequest(token: String): Int = {
    val result = WS.clientUrl("http://localhost:9997/endpoint")
      .withHeaders("Content-Type" -> "application/json")
      .post(s"""{"token": "$token", "payload": "stuff to process"}""")
      .map(response => response.status)

    wait(result)
  }


}

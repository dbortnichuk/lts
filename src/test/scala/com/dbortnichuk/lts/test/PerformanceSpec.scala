package com.dbortnichuk.lts.test

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import com.dbortnichuk.lts.Launcher
import org.scalatestplus.play.PlaySpec
import spray.http.StatusCodes

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration


class PerformanceSpec extends PlaySpec with TestConfig with TestUtil {

  private val launcher = new Launcher(getConfig("application-integration.conf"))

  "Lst" should {
    "succeed with 90 - 110 % of requests for two users with their respective Sla during 20 second test" in {
      val testDurationSec = 20
      val testIntervalSec = 1
      val requestPerInterval = 200
      val expectedSuccessfulRequestPerInterval = 150
      //Sla("Jean-Claude Van Damme", 50) + Sla("Dolph Lundgren", 100)
      val results = new ArrayBuffer[Int]()
      val actorSystem = ActorSystem("test-system")
      val scheduler = actorSystem.scheduler
      val testActor = actorSystem.actorOf(Props(new TestActor(testDurationSec, requestPerInterval)))
      implicit val executor = actorSystem.dispatcher

      scheduler.schedule(
        initialDelay = Duration(testIntervalSec * 2, TimeUnit.SECONDS),
        interval = Duration(testIntervalSec, TimeUnit.SECONDS),
        testActor, Generate(results))

      Thread.sleep(testDurationSec * 1100)
      // wait for more than testDurationSec
      val expectedSuccessfulRequests = expectedSuccessfulRequestPerInterval * testDurationSec
      val successfulRequests = results.count(_ == StatusCodes.OK.intValue)
      Math.abs(successfulRequests - expectedSuccessfulRequests) < delta(expectedSuccessfulRequests, 10) mustBe (true)
    }

  }

}

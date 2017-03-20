package com.dbortnichuk.lts.test

import java.io.File

import com.dbortnichuk.lts.Launcher
import org.scalatestplus.play.PlaySpec
import play.api.libs.ws.WS
import spray.http.StatusCodes


class PerformanceComparisonSpec extends PlaySpec with RestClient with TestConfig with TestUtil {
  // implemented 2 similar tests as execution may differ on different environments

  private val launcherWithThrottling = new Launcher(getConfig("application-enabled.conf"))
  private val launcherWithoutThrottling = new Launcher(getConfig("application-disabled.conf"))

  import com.dbortnichuk.lts.SlaService._

  private val initCaches = {
    Thread.sleep(1000)
    (1 to 5).map(_ => createRequest(TokenChristmas, "9999"))
    (1 to 5).map(_ => createRequest(TokenChristmas, "9998"))
  }

  "Lst" should {
    "have throttling response time overhead under 12 % " in {
      test(12)
    }

    "have throttling response time overhead under 6 %" in {
      test(6)
    }
  }

  def test(deltaPercent: Int): Unit = {
    Thread.sleep(1000)

    val startWithThrottling = System.currentTimeMillis()
    (1 to 1000).map(_ => createRequest(TokenChristmas, "9999")).count(_ == StatusCodes.OK.intValue)
    val endWithThrottling = System.currentTimeMillis() - startWithThrottling
    println(s"Executed with throttling in: $endWithThrottling ms")

    val startWithOutThrottling = System.currentTimeMillis()
    (1 to 1000).map(_ => createRequest(TokenChristmas, "9998")).count(_ == StatusCodes.OK.intValue)
    val endWithOutThrottling = System.currentTimeMillis() - startWithOutThrottling
    println(s"Executed without throttling in: $endWithOutThrottling ms")

    startWithThrottling - startWithOutThrottling < delta(endWithThrottling, deltaPercent) mustBe true
  }

  def createRequest(token: String, port: String): Int = {
    val result = WS.clientUrl(s"http://localhost:$port/endpoint")
      .withHeaders("Content-Type" -> "application/json")
      .post(s"""{"token": "$token", "payload": "stuff to process"}""")
      .map(response => response.status)

    wait(result)
  }


}

package com.dbortnichuk.lts.test


import com.dbortnichuk.lts.{Launcher}

import org.scalatestplus.play.PlaySpec
import play.api.libs.ws.WS
import spray.http.StatusCodes


class LstIntegrationSpec extends PlaySpec with RestClient with TestConfig {
  //sometimes may fail due to random timeout while querying Sla service

  private val launcher = new Launcher(getConfig("application-integration.conf"))

  import com.dbortnichuk.lts.SlaService._

  "Lst" should {
    "succeed only 1 time for unauthorized due to grace rps" in {
      //graceRps == 1
      (1 to 2).map(_ => createRequest("unknown")).count(_ == StatusCodes.OK.intValue).mustBe(1)
    }
    "fail only 1 time for particular user due to sla" in {
      //Sla("Arnold Schwarzenegger", 5)
      (1 to 6).map(_ => createRequest(TokenArny)).count(_ == StatusCodes.TooManyRequests.intValue).mustBe(1)
    }
    "succeed only 3 times for same user with different tokens" in {
      //Sla("Sylvester Stallone", 3)
      (1 to 4).map(idx => if (idx % 2 == 0) createRequest(TokenSly1) else createRequest(TokenSly2)).count(_ == StatusCodes.OK.intValue).mustBe(3)
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

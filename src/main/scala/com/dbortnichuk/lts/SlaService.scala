package com.dbortnichuk.lts

import com.dbortnichuk.lts.Protocol.Sla

import scala.concurrent.{Future, blocking}
import scala.concurrent.ExecutionContext.Implicits._
import scala.util.Random

trait SlaService {

  def getSlaByToken(token: String): Future[Sla]

}

object SlaService {
  val TokenSly1 = "token123"
  val TokenSly2 = "token456"
  val TokenArny = "token000"
  val TokenJCVD = "token111"
  val TokenHans = "token222"
  val TokenChristmas = "token333"
}

class SlaServiceImpl extends SlaService {

  import SlaService._

  val database =
    Map(
      TokenSly1 -> Sla("Sylvester Stallone", 3),
      TokenSly2 -> Sla("Sylvester Stallone", 3),
      TokenArny -> Sla("Arnold Schwarzenegger", 5),
      TokenJCVD -> Sla("Jean-Claude Van Damme", 50),
      TokenHans -> Sla("Dolph Lundgren", 100),
      TokenChristmas -> Sla("Jason Statham", 5000)
    )


  override def getSlaByToken(token: String) = Future {
    blocking {
      Thread.sleep(250 + (if (Random.nextBoolean()) +1 * Random.nextInt(2) else -1 * Random.nextInt(51))) // costly call, 200 - 251 ms
      database(token)
    }
  }


}


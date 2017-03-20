package com.dbortnichuk.lts.test

import ch.qos.logback.classic.{Level, Logger}
import org.slf4j.LoggerFactory
import play.api.libs.ws.ning.{NingAsyncHttpClientConfigBuilder, NingWSClient}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

trait RestClient {
  implicit val executor = ExecutionContext.Implicits.global

  implicit val client = RestClient.client

  def wait[T](feature: Future[T], duration: Duration = 3.minutes): T = Await.result(feature, duration)
}

object RestClient {

  val client = {
    val root = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger]
    root.setLevel(Level.OFF) // disable logging of each request in NettyRequestSender
    new NingWSClient(new NingAsyncHttpClientConfigBuilder().build())
  }

}
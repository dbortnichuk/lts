package com.dbortnichuk.lts

import java.io.File

import akka.actor._
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import spray.can.Http

import scala.concurrent.duration._

object Main extends App {

  val config = if (args.length > 0) ConfigFactory.parseFile(new File(args(0))).resolve() else ConfigFactory.load()
  val launcher = new Launcher(config)

}

class Launcher(config: Config) {

  private val host = config.getString("http.host")
  private val port = config.getInt("http.port")
  implicit val system = ActorSystem("lts-system")

  val api = system.actorOf(Props(new RestInterface(config)), "httpInterface")

  implicit val executionContext = system.dispatcher
  implicit val timeout = Timeout(10 seconds)

  IO(Http).ask(Http.Bind(listener = api, interface = host, port = port))
    .mapTo[Http.Event]
    .map {
      case Http.Bound(address) =>
        println(s"REST interface bound to $address")
      case Http.CommandFailed(cmd) =>
        println("REST interface could not bind to " +
          s"$host:$port, ${cmd.failureMessage}")
        system.shutdown()
    }


}

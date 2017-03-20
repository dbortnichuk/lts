package com.dbortnichuk.lts

import akka.actor._
import com.dbortnichuk.lts.Protocol._
import com.typesafe.config.Config
import spray.httpx.SprayJsonSupport._
import spray.http.StatusCodes
import spray.routing._

import scala.language.postfixOps

class RestInterface(conf: Config) extends HttpServiceActor with RestApi {

  def receive = runRoute(routes)

  override val config = conf
}

trait RestApi extends HttpService with ActorLogging {
  actor: Actor =>

  val config: Config

  private val processor = context.actorOf(Props(new ProcessorActor(config)))

  def routes: Route =
    pathPrefix("endpoint") {
      pathEnd {
        post {
          entity(as[UserRequest]) { userRequest =>
            requestContext =>
              Thread.sleep(4) // simulate response time 4 - 5 ms
              processor ! Process(userRequest, requestContext)
          }
        }
      }
    } ~
      pathPrefix("command") {
        pathEnd {
          post {
            entity(as[Command]) { command =>
              requestContext =>
                if (command.command == "shutdown") {
                  requestContext.complete(StatusCodes.OK, "shutting down...")
                  Thread.sleep(1000)
                  context.system.shutdown()
                }
                else requestContext.complete(StatusCodes.NotImplemented, s"${command.command} not implemented")
            }
          }
        }
      }


}






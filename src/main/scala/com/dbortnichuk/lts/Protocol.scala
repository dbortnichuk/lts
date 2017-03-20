package com.dbortnichuk.lts

object Protocol {

  import spray.json._

  case class Sla(user: String, rps: Int)

  case class UserRequest(token: Option[String], payload: String)

  object UserRequest extends DefaultJsonProtocol {
    implicit val format = jsonFormat2(UserRequest.apply)
  }

  case object RequestsLimitExceeded

  case class ProcessedMessage(processed: String)

  object ProcessedMessage extends DefaultJsonProtocol {
    implicit val format = jsonFormat1(ProcessedMessage.apply)
  }

  case class Command(command: String)

  object Command extends DefaultJsonProtocol {
    implicit val format = jsonFormat1(Command.apply)
  }

  case class SystemResponse(response: ProcessedMessage)

  object SystemResponse extends DefaultJsonProtocol {
    implicit val format = jsonFormat1(SystemResponse.apply)
  }

}

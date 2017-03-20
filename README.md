# Throttling Service


## Usage

**git clone** 

**sbt run** - to run the service instance

**sbt test** - to run the tests

## Comments
 * Not adhered to contract if that was completely required
 ```
 trait ThrottlingService {
 val graceRps:Int // configurable
 val slaService: SlaService // use mocks/stubs for testing
 // Should return true if the request is within allowed RPS.
 def isRequestAllowed(token:Option[String]): Boolean
 }
 ```
 
 used instead 
 
 ```
 ThrottlingServiceActor.scala
 ```
 
 * Not implemented (5), unclear task. In case limit reached during first 10% of time slot allowing more after will break the RPS concept itself.
    In case flat distribution of requests is required, e.g. if allow only 10% of requests during 10% of time slot otherwise block until next time slot
     and drop request in case overall limit reached - there is a problem with response time degradation in case of relatively small amount of allowed requests
      next request batch will have to wait around 10x of its native response time. 
 

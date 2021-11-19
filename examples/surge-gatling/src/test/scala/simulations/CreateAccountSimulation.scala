package simulations

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import org.slf4j.LoggerFactory

import java.util.UUID
import scala.concurrent.duration._
import scala.language.postfixOps

class CreateAccountSimulation extends Simulation {

  private val log = LoggerFactory.getLogger(getClass)

  val scalaAppUrl: String = System.getenv("SCALA_APP_URL")

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(scalaAppUrl)
    .header("content-Type", "application/json")

  private def createRequestBody(
      initialBalance: Int
  ): String = {
    s"""{
       |    "accountNumber": "${UUID.randomUUID()}",
       |    "accountOwner": "Jane Doe",
       |     "securityCode": "12345678910",
       |     "initialBalance": $initialBalance
       |}""".stripMargin
  }

  val scn: ScenarioBuilder = scenario("create user account")
    .exec(
      http("create account request")
        .post("/bank-accounts/create")
        .body(StringBody(createRequestBody(1000)))
        .check(
          jsonPath("$.accountNumber")
            .saveAs("aggregateId")
        )
    )
    .exec(session => {
      log.info("User account response")
      log.info(session("aggregateId").as[String])
      session
    })
    .pause(500 microsecond)
    .exec(
      http("Get Bank account state")
        .get("/bank-accounts/${aggregateId}")
        .check(status.is(200), bodyString.saveAs("balance"))
    )
    .exec(session => {
      log.info("Account Balance:")
      log.info(session("balance").as[String])
      session
    })

  setUp(
    scn
      .inject(atOnceUsers(1), rampUsers(15).during(60.seconds))
      .protocols(httpProtocol)
  )

}

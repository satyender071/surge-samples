package simulations

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import java.util.UUID
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class DebitAccountSimulation extends Simulation {
  val scalaAppUrl: String = System.getenv("SCALA_APP_URL")
  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(scalaAppUrl)
    .header("content-Type", "application/json")

  private def createRequestBody(
      initialBalance: Int,
      accountNumber: UUID
  ): String = {
    s"""{
       |    "accountNumber": "$accountNumber",
       |    "accountOwner": "Jane Doe",
       |     "securityCode": "12345678910",
       |     "initialBalance": $initialBalance
       |}""".stripMargin
  }

  private def debitRequestBody(initialBalance: Int): String = {
    s"""{
       |     "amount": $initialBalance
       |}""".stripMargin
  }

  val scn: ScenarioBuilder = scenario("create user account")
    .exec(
      http("create account request")
        .post("/bank-accounts/create")
        .body(StringBody(createRequestBody(10000, UUID.randomUUID())))
        .check(
          status is (200),
          jsonPath("$.accountNumber")
            .saveAs("aggregateId")
        )
    )
    .exec(session => {
      println("User account response")
      println(session("aggregateId").as[String])
      session
    })
    .pause(500 microsecond)
    .exec(
      http("Debit Account Request")
        .put("/bank-accounts/debit/${aggregateId}")
        .body(StringBody(debitRequestBody(100)))
        .check(status.is(200), bodyString.saveAs("balance"))
    )
    .exec(session => {
      println("Account Balance after debit:")
      println(session("balance").as[String])
      session
    })

  setUp(
    scn
      .inject(atOnceUsers(1), rampUsers(5).during(5.seconds))
      .protocols(httpProtocol)
  )

}

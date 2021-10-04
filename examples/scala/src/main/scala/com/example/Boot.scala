// Copyright © 2017-2021 UKG Inc. <https://www.ukg.com>

package com.example

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import com.example.account.BankAccount
import com.example.http.request.CreateAccountRequest
import com.example.http.serializer.BankAccountRequestSerializer
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.slf4j.{LoggerFactory, MDC}
import surge.scaladsl.common.{CommandFailure, CommandSuccess}
import com.example.http.request.RequestToCommand._
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import scala.io.StdIn
import surge.internal.utils.MdcExecutionContext.mdcExecutionContext

object Boot extends App with PlayJsonSupport with BankAccountRequestSerializer {

  implicit val system = BankAccountEngine.surgeEngine.actorSystem
  private val log = LoggerFactory.getLogger(getClass)
  private val config = ConfigFactory.load()

  val route =
    pathPrefix("bank-accounts") {
      concat(
        path("create") {
          post {
            entity(as[CreateAccountRequest]) { request =>
              val createAccountCommand = requestToCommand(request)
              MDC.put("account_number", createAccountCommand.accountNumber.toString)
              val createdAccountF: Future[Option[BankAccount]] = BankAccountEngine.surgeEngine.aggregateFor(createAccountCommand.accountNumber).sendCommand(createAccountCommand).map {
                case CommandSuccess(aggregateState) => aggregateState
                case CommandFailure(reason)         => throw reason
              }

              onSuccess(createdAccountF) {
                case Some(account) => complete(account)
                case None => complete(StatusCodes.InternalServerError)
              }
            }
          }
        },
        path(JavaUUID) { uuid =>
          get {
            MDC.put("account_number", uuid.toString)
            val accountStateF = BankAccountEngine.surgeEngine.aggregateFor(uuid).getState
            log.info("Get account owner's state ")
            onSuccess(accountStateF) {
              case Some(accountState) => complete(accountState)
              case None => complete(StatusCodes.NotFound)
            }
          }
        }
      )
    }


  val host = config.getString("http.host")
  val port = config.getInt("http.port")
  val bindingFuture = Http().newServerAt(host, port).bind(route)

  log.info(s"Server is running on http://$host:$port")

}

// Copyright © 2017-2021 UKG Inc. <https://www.ukg.com>

import sbt._

object Dependencies extends AutoPlugin {
  object autoImport {

    object Akka {
      val http = "com.typesafe.akka" %% "akka-http" % "10.2.6"
    }

    val surge = "com.ukg" %% "surge-engine-command-scaladsl" % "0.5.37-SNAPSHOT"
    val akkaHttpPlayJson = "de.heikoseeberger" %% "akka-http-play-json" % "1.38.2"
  }
}

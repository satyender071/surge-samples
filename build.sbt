// Copyright © 2017-2021 UKG Inc. <https://www.ukg.com>

name := "surge-samples"

version := "0.1"

ThisBuild / scalaVersion := "2.13.5"

publish / skip := true

lazy val `surge-scala-sample` = (project in file("examples/scala"))
  .settings(libraryDependencies ++= Seq(Akka.http, akkaHttpPlayJson, surge, gatling, gatlingFramework), publish / skip := true)
  .enablePlugins(JavaServerAppPackaging)

lazy val `surge-gatling-sample` = (project in file("examples/surge-gatling"))
  .settings(libraryDependencies ++= Seq(gatling, gatlingFramework), publish / skip := true)
  .enablePlugins(GatlingPlugin)


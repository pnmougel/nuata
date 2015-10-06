import play.sbt.PlayImport._
import play.sbt.PlayScala
import play.sbt.routes.RoutesKeys._
import sbt.Keys._
import sbt._

object Build extends Build {
  val appName = "nuata"

  val dependencies = Seq(
    jdbc,
    cache,
    ws,
    evolutions,
    filters,
    specs2 % Test,
    "com.typesafe.play" %% "anorm" % "2.4.0",
    "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
    "org.scalaj" %% "scalaj-http" % "1.1.5",
    "io.spray" %%  "spray-json" % "1.3.2"
  )

  lazy val crawlers = (project in file("crawlers"))
    .settings(
      libraryDependencies ++= dependencies
    )

  lazy val root = (project in file(".")).enablePlugins(PlayScala)
    .settings(
      name := appName,
      organization := "org.nuata",
      version := "1.0",
      scalaVersion := "2.11.6",
      libraryDependencies ++= dependencies,
      resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",

      // Play provides two styles of routers, one expects its actions to be injected, the
      // other, legacy style, accesses its actions statically.
      routesGenerator := InjectedRoutesGenerator
    )
}
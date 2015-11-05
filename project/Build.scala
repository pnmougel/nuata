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
    specs2,
    // specs2 % Test,
    "com.typesafe.play" %% "anorm" % "2.4.0",
    "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
    "org.scalaj" %% "scalaj-http" % "1.1.5",
    "io.spray" %%  "spray-json" % "1.3.2",

    // Joda time
    "joda-time" % "joda-time" % "2.8.2",

    // Json4s
//    "org.json4s" %% "json4s-native" % "3.3.0",
    "org.json4s" %% "json4s-jackson" % "3.3.0",
    "org.json4s" % "json4s-ext_2.10" % "3.3.0",
    "com.github.tototoshi" %% "play-json4s-jackson" % "0.4.2",
    "com.github.tototoshi" %% "play-json4s-test-jackson" % "0.4.2" % "test",

    // Json extra
    "io.megl" % "play-json-extra_2.11" % "2.4.3",

    // Elasticsearch
    "com.sksamuel.elastic4s" %% "elastic4s-core" % "1.7.4",
    "com.sksamuel.elastic4s" % "elastic4s-jackson_2.11" % "1.7.4",

    // Language detection
    "com.optimaize.languagedetector" % "language-detector" % "0.5"
  )

  lazy val sampleStringTask = taskKey[String]("A sample string task.")
  sampleStringTask := System.getProperty("user.home")

  lazy val hello = taskKey[Unit]("Prints 'Hello World'")

  def findTestFolders(basePath: File) : List[File] = {
    var testFolders = List[File]()
    for(file <- basePath.listFiles(); if(file.isDirectory)) {
      if(file.getName == "test") {
        testFolders = file :: testFolders
      }

      findTestFolders(file)
    }
    testFolders
  }

  val commonSettings = Seq(
    organization := "org.nuata",
    version := "1.0",
    scalaVersion := "2.11.6",
    ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }
  )

  lazy val root = (project in file("."))
    .enablePlugins(PlayScala)
    .settings(commonSettings: _*)
    .settings(
      name := appName,
      libraryDependencies ++= dependencies,
      resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",

      javaOptions += "-XX:MaxMetaspaceSize=512m",

      scalacOptions in Test ++= Seq("-Yrangepos"),
      // Play provides two styles of routers, one expects its actions to be injected, the
      // other, legacy style, accesses its actions statically.
      routesGenerator := InjectedRoutesGenerator,

      scalaSource in Test := baseDirectory.value / "app",
      sourceDirectories in Test ++= baseDirectory.value / "test" :: findTestFolders(baseDirectory.value / "app"),
      includeFilter in Test := HiddenFileFilter || "*Spec.scala",

      // Remove documentation from the build
      sources in (Compile, doc) := Seq.empty,
      publishArtifact in (Compile, packageDoc) := false

      // Deploy config
    )


  lazy val crawlers = (project in file("crawlers"))
    .settings(commonSettings: _*)
    .settings(
      libraryDependencies ++= dependencies
    )
//    .aggregate(root)
    .dependsOn(root)


  val inseeDependencies = Seq(
    "org.scalaj" %% "scalaj-http" % "1.1.5",
    "org.jsoup" % "jsoup" % "1.7.2",
    "com.typesafe.play" %% "play-json" % "2.3.4"
  )

  lazy val insee = (project in file("insee"))
    .settings(
      name := appName,
      organization := "org.nuata",
      version := "0.1",
      libraryDependencies ++= inseeDependencies
    )
}
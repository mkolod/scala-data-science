import sbt._
import Keys._

object WorkshopBuild extends Build {

  lazy val baseSettings = Defaults.defaultSettings ++ Seq(
    version := "0.1",
    organization := "us.marek",
    scalaVersion := "2.11.6",
    name := "scala_data_science",
    fork := true,
    fork in Test := false,
    parallelExecution := true,
    parallelExecution in Test := false,
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
    javaOptions ++= Seq("-Xmx4G", "-Xms256M"),
    scalacOptions ++= Seq(
      "-optimize",
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:experimental.macros",
      "-unchecked",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Xfuture",
      "-Yinline-warnings"
    ),
    ivyScala := ivyScala.value map {
      _.copy(overrideScalaVersion = true)
    },
    pollInterval := 1000,
    resolvers ++= Seq("Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
      "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
      "Akka Repository" at "http://repo.akka.io/releases/",
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
      "Twitter Repository" at "http://maven.twttr.com/",
      "Spark Packages Repo" at "http://dl.bintray.com/spark-packages/maven"
    ),
    libraryDependencies ++= Seq(
      "org.scalanlp" %% "breeze" % "0.11.2",
      "org.scalanlp" %% "breeze-natives" % "0.11.2",
      "com.quantifind" %% "wisp" % "0.0.4",
      "org.scalatest"     %  "scalatest_2.10"  %  "2.2.1" % "test",
      "org.apache.spark" %%  "spark-core"  %  "1.4.0"
    ),
    shellPrompt <<= name(name => { state: State =>
      object devnull extends ProcessLogger {
        def info(s: => String) {}

        def error(s: => String) {}

        def buffer[T](f: => T): T = f
      }
      val current = """\*\s+(\w+)""".r
      def gitBranches = ("git branch --no-color" lines_! devnull mkString)
      "%s:%s>" format(
        name,
        current findFirstMatchIn gitBranches map (_.group(1)) getOrElse "-"
        )
    }),
    connectInput in run := true
  )

  lazy val root = project
    .in(file("."))
    .settings(baseSettings: _*)

}
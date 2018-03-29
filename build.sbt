import sbt.Keys._

val projectName = "apollon"
val projectVersion = "3.0.0"
val projectScalaVersion = "2.11.7"
val _organization = "com.geishatokyo." + projectName
val appPort = "9000"
val dependPlay = "com.typesafe.play" %% "play-server" % "2.4.6"

val forLibrary = Seq(
  dependPlay,
  "org.scala-lang" % "scala-reflect" % projectScalaVersion,
  "org.slf4j" % "slf4j-simple" % "1.7.14",
  "org.json4s" %% "json4s-native" % "3.3.0"
)

val forServer = Seq(dependPlay)

val forTestHttpClient = Seq(
  "org.json4s" %% "json4s-native" % "3.3.0",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.3"
)

/** override run command to run child play project. */
def runChildPlayServer = Command.command("run")( state => {
  val subState = Command.process(s"project server",state)
  Command.process(s"run ${appPort}",subState)
  state
})

def runHttpClient = Command.command("runClient",Help("runClient" -> "run http client",Map.empty[String,String]))( state => {
  val subState = Command.process("project httpClient",state)
  Command.process("console",subState)
  state
})

def assemblyServer = Command.args("assembly","<args>")( (state,args) => {
  val subState = Command.process(s"project server",state)
  Command.process("assembly",subState)
  state
})

/**
  * Lower case support
  *
  * @return
  */
lazy val apollon = (project in file("."))
  .settings(commonSettings : _*)
  .settings(
    description := "parent project for " + projectName,
    commands ++= Seq(
      runChildPlayServer,
      assemblyServer,
      runHttpClient),
    scalacOptions += """-Dfile.encoding=utf8"""
  ).aggregate(library,server)

lazy val commonSettings = Defaults.coreDefaultSettings ++ Seq(
  version := projectVersion,
  organization := _organization,
  scalaVersion := projectScalaVersion,
  /*
  scalacOptions ++= Seq(
    "-deprecation"
    , "-feature"
    , "-unchecked"
    //      ,"-Xlint"
    , "-Ywarn-dead-code"
    , "-Ywarn-unused"
    , "-Ywarn-unused-import"
    // 警告をエラーにする（お好みに応じて）
    //      , "-Xfatal-warnings"
  ),
  */
  resolvers ++= Seq(Resolver.mavenLocal),
  publishTo := {
    if (System.getenv("PLAY_HOME") != null){
      Some( Resolver.file("playRepository", Path(System.getenv("PLAY_HOME")) / "repository" / "cache")(
        Patterns(Nil, "[organisation]/[module](_[scalaVersion])/[type]s/[artifact]-[revision](-[classifier]).[ext]"  :: Nil, false)
      ))
    }else{
      None
    }
  }
)

lazy val library = (project in file(s"./${projectName}-library"))
  .settings(commonSettings : _*)
  .settings(
    description := "Library for " + projectName,
    libraryDependencies ++= forLibrary,
    assemblyMergeStrategy in assembly := {
      case "play/core/server/ServerWithStop.class" => MergeStrategy.first
      case PathList(ps@_*) if ps.last endsWith ".xml" => MergeStrategy.first
      case PathList(ps@_*) if ps.last endsWith ".class" => MergeStrategy.first
      case PathList(ps@_*) if ps.last endsWith ".html" => MergeStrategy.first
      case other => (assemblyMergeStrategy in assembly).value(other)
    }
  )

lazy val server = (project in file(s"./${projectName}-server"))
  .enablePlugins(PlayScala)
  .settings(
    version := projectVersion,
    organization := _organization,
    checksums in update := Nil, // for maven compatible in Windows
    scalaVersion := projectScalaVersion,
    libraryDependencies ++= forServer,
    routesGenerator := InjectedRoutesGenerator,
    initialCommands := """System.setProperty( "file.encoding", "UTF-8" )""",
    scalacOptions += """-Dfile.encoding=utf8""",
    mainClass in assembly := Some("play.core.server.ProdServerStart"),
    fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value),
    // Take the first ServerWithStop because it's packaged into two jars
    assemblyMergeStrategy in assembly := {
      case PathList(ps @ _*) if ps.last contains "application.conf" => MergeStrategy.discard
      case PathList(ps @ _*) if ps.last contains "logger.xml" => MergeStrategy.discard
      case "play/core/server/ServerWithStop.class" => MergeStrategy.first
      case PathList(ps @ _*) if ps.last endsWith ".xml" => MergeStrategy.first
      case PathList(ps @ _*) if (ps.last endsWith ".class")=> MergeStrategy.first
      case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
      case other => (assemblyMergeStrategy in assembly).value(other)
    }
  ).dependsOn(library).aggregate(library)

lazy val httpClient = (project in file("./http-client"))
  .settings(commonSettings : _*)
  .settings(
    description := "Application to test " + projectName +  " server",
    libraryDependencies ++= forTestHttpClient,
    scalacOptions += """-Dfile.encoding=utf8""",
    initialCommands :=
      """import com.geishatokyo.apollon.util.json.JsonHelper
        |import com.geishatokyo.apollon.model.api._
        |import com.geishatokyo.httpclient._
      """.stripMargin.format(projectName)
  ).dependsOn(library)

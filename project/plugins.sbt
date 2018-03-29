// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
// Exclude commons-logging because it conflicts with the jcl-over-slf4j
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.6" exclude("commons-logging", "commons-logging"))

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.13.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

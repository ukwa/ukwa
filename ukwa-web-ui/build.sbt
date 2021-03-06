name := """ukwa-web-ui"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "com.typesafe.akka" %% "akka-actor" % "2.3.13",
  "org.mapdb" % "mapdb" % "2.0-beta10",
  "org.netpreserve.commons" % "webarchive-commons" % "1.1.6",
  "org.apache.httpcomponents" % "httpclient" % "4.3.6",
  "org.apache.httpcomponents" % "httpcore" % "4.3.3"
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

// Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
EclipseKeys.preTasks := Seq(compile in Compile)

fork in run := true

resolvers += "Cloudera Hadoop" at "https://repository.cloudera.com/artifactory/cloudera-repos/"

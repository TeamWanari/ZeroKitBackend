name := "tresorit-backend"
organization := "com.wanari.tresorit"
version := "1.0.1"
scalaVersion := "2.12.0"

lazy val akkaHttpVersion = "10.0.4"

enablePlugins(JavaServerAppPackaging)

mainClass in Compile := Some("com.wanari.tresorit.HttpServer")

packageName in Docker := "tresorit-backend-docker"

dockerExposedPorts := Seq(8080)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"             % akkaHttpVersion,
	"com.fasterxml.jackson.datatype" % "jackson-datatype-json-org" % "2.8.7"
)

fork in run := true

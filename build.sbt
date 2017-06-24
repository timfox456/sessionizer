name := "sessionizer"

version := "0.1"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.5.2" % "provided",
  "org.apache.spark" %% "spark-streaming" % "1.5.2"  % "provided",
  "com.github.nscala-time" %% "nscala-time" % "2.6.0",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
)


name := "Img2Pdf"

version := "0.1"

scalaVersion := "2.9.2"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.5"

//libraryDependencies += "com.typesafe.akka" % "akka-testkit" % "2.1"

//libraryDependencies += "com.typesafe" % "play-mini_2.9.1" % "2.0.1"

libraryDependencies += "com.itextpdf" % "itextpdf" % "5.1.3"

//Test

libraryDependencies += "org.scalatest" %% "scalatest" % "1.8" % "test"

libraryDependencies += "junit" % "junit" % "4.5" % "test"



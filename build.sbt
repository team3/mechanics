name := "mechanics"

version := "1.0"

lazy val `mechanics` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

val reactiveMongoVer = "0.11.14"

libraryDependencies ++= Seq(jdbc, cache, ws, specs2 % Test)

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % reactiveMongoVer
)

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  
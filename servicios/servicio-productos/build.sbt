
name := "servicio-productos"

version := "0.1.0"

scalaVersion := "2.13.12"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % "0.23.23",
  "org.http4s" %% "http4s-blaze-server" % "0.23.23",
  "org.http4s" %% "http4s-circe" % "0.23.23",
  "io.circe" %% "circe-generic" % "0.14.6",
  "org.typelevel" %% "cats-effect" % "3.5.2",
  "org.postgresql" % "postgresql" % "42.6.0",
  "com.zaxxer" % "HikariCP" % "5.0.1",
  "org.tpolecat" %% "doobie-core" % "1.0.0-RC2",
  "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC2",
  "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC2"
)

assembly / mainClass := Some("ProductosService")


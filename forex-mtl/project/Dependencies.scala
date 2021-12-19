import sbt._

object Dependencies {

  object Versions {
    val cats                = "2.5.0"
    val catsEffect          = "2.4.1"
    val fs2                 = "2.5.4"
    val http4s              = "0.21.22"
    val circe               = "0.13.0"
    val pureConfig          = "0.14.1"
    val scaffeine           = "5.1.1"

    val kindProjector       = "0.10.3"
    val logback             = "1.2.3"
    val scalaCheck          = "1.15.3"
    val scalaTest           = "3.2.7"
    val catsScalaCheck      = "0.3.0"
    val wireMock            = "2.27.2"
    val mockito             = "3.12.4"
    val mockitoSugar        = "1.0.0-M2"
  }

  object Libraries {
    def circe(artifact: String): ModuleID = "io.circe"    %% artifact % Versions.circe
    def http4s(artifact: String): ModuleID = "org.http4s" %% artifact % Versions.http4s

    lazy val cats                = "org.typelevel"         %% "cats-core"                  % Versions.cats
    lazy val catsEffect          = "org.typelevel"         %% "cats-effect"                % Versions.catsEffect
    lazy val fs2                 = "co.fs2"                %% "fs2-core"                   % Versions.fs2

    lazy val http4sDsl           = http4s("http4s-dsl")
    lazy val http4sServer        = http4s("http4s-blaze-server")
    lazy val http4sCirce         = http4s("http4s-circe")
    lazy val http4sClient        = http4s("http4s-blaze-client")
    lazy val circeCore           = circe("circe-core")
    lazy val circeGeneric        = circe("circe-generic")
    lazy val circeGenericExt     = circe("circe-generic-extras")
    lazy val circeParser         = circe("circe-parser")
    lazy val pureConfig          = "com.github.pureconfig"  %% "pureconfig"                 % Versions.pureConfig
    lazy val scaffeine           = "com.github.blemale"     %% "scaffeine"                  % Versions.scaffeine

    // Compiler plugins
    lazy val kindProjector       = "org.typelevel"          %% "kind-projector"             % Versions.kindProjector

    // Runtime
    lazy val logback             = "ch.qos.logback"         %  "logback-classic"            % Versions.logback

    // Test
    lazy val scalaTest           = "org.scalatest"          %% "scalatest"                  % Versions.scalaTest
    lazy val scalaCheck          = "org.scalacheck"         %% "scalacheck"                 % Versions.scalaCheck
    lazy val catsScalaCheck      = "io.chrisdavenport"      %% "cats-scalacheck"            % Versions.catsScalaCheck
    lazy val wireMock            = "com.github.tomakehurst" % "wiremock"                    % Versions.wireMock
    lazy val mockito             = "org.mockito"            % "mockito-core"                % Versions.mockito
    lazy val mockitoSugar        = "org.scalatestplus"      %% "scalatestplus-mockito"      % Versions.mockitoSugar
  }

}

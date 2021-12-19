package forex.services.rates.interpreters

import cats.effect.{ContextShift, IO}
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import forex.config.ApplicationConfig
import forex.domain.Rate.Pair
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.services.rates.errors.Error.{NonFatalRateServiceError, OneFrameLookupFailed, ResponseDecodeFailure}
import org.http4s.client.blaze.BlazeClientBuilder
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfter, EitherValues}
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import java.time.OffsetDateTime
import scala.concurrent.ExecutionContext

class OneFrameInterpreterSpec extends AnyFlatSpec with Matchers with EitherValues with BeforeAndAfter {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)

  val config = ConfigSource.resources("env/test.conf").at("app").loadOrThrow[ApplicationConfig]

  val wireMockServer = new WireMockServer(
    wireMockConfig()
      .port(config.oneFrame.port)
  )

  before {
    wireMockServer.start()
  }

  after {
    wireMockServer.stop()
  }

  "OneFrameInterpreter" should "process rate data correctly" in {
    val fakeResponse =
      """
        |  [{
        |    "from" : "USD",
        |    "to" : "JPY",
        |    "bid" : 0.6118225421857174,
        |    "ask" : 0.8243869101616611,
        |    "price" : 0.71810472617368925,
        |    "time_stamp" : "2021-12-19T16:18:51.813Z"
        |  }]
        |""".stripMargin
    val expectedRateResult = Rate(
      Pair(Currency.USD, Currency.JPY),
      Price(0.71810472617368925),
      Timestamp(OffsetDateTime.parse("2021-12-19T16:18:51.813Z"))
    )

    println(config.oneFrame)

    wireMockServer.stubFor(
      WireMock.get(urlEqualTo("/rates?pair=USDJPY"))
        .withHeader("token", equalTo(config.oneFrame.token))
        .withHost(equalTo(config.oneFrame.host))
        .withPort(config.oneFrame.port)
        .willReturn(okJson(fakeResponse))
    )

    val resultIO = BlazeClientBuilder[IO](ec).resource.use {client =>
      val oneFrameInterpreter = OneFrameInterpreter[IO](config, client)
      oneFrameInterpreter.get(Pair(Currency.USD, Currency.JPY))
    }
    val result = resultIO.unsafeRunSync()
    result shouldBe Right(expectedRateResult)
  }

  it should "process error message correctly" in {
    val fakeResponse =
      """
        |  {
        |    "error" : "some error"
        |  }
        |""".stripMargin
    val expectedError = OneFrameLookupFailed("some error")

    wireMockServer.stubFor(
      WireMock.get(urlEqualTo("/rates?pair=USDJPY"))
        .withHeader("token", equalTo(config.oneFrame.token))
        .withHost(equalTo(config.oneFrame.host))
        .withPort(config.oneFrame.port)
        .willReturn(okJson(fakeResponse))
    )

    val resultIO = BlazeClientBuilder[IO](ec).resource.use {client =>
      val oneFrameInterpreter = OneFrameInterpreter[IO](config, client)
      oneFrameInterpreter.get(Pair(Currency.USD, Currency.JPY))
    }
    val result = resultIO.unsafeRunSync()
    result shouldBe Left(expectedError)
  }

  it should "process invalid response correctly" in {
    val fakeResponse =
      """
        |  {
        |    "unknownField" : "some error"
        |  }
        |""".stripMargin

    wireMockServer.stubFor(
      WireMock.get(urlEqualTo("/rates?pair=USDJPY"))
        .withHeader("token", equalTo(config.oneFrame.token))
        .withHost(equalTo(config.oneFrame.host))
        .withPort(config.oneFrame.port)
        .willReturn(okJson(fakeResponse))
    )

    val resultIO = BlazeClientBuilder[IO](ec).resource.use {client =>
      val oneFrameInterpreter = OneFrameInterpreter[IO](config, client)
      oneFrameInterpreter.get(Pair(Currency.USD, Currency.JPY))
    }
    val result = resultIO.unsafeRunSync()
    result.isLeft shouldBe true
    result.left.value shouldBe a[ResponseDecodeFailure]
  }

  it should "process unexpected status code correctly" in {
    wireMockServer.stubFor(
      WireMock.get(urlEqualTo("/rates?pair=JPYUSD"))
        .withHeader("token", equalTo(config.oneFrame.token))
        .withHost(equalTo(config.oneFrame.host))
        .withPort(config.oneFrame.port)
        .willReturn(badRequest())
    )

    val resultIO = BlazeClientBuilder[IO](ec).resource.use {client =>
      val oneFrameInterpreter = OneFrameInterpreter[IO](config, client)
      oneFrameInterpreter.get(Pair(Currency.JPY, Currency.USD))
    }
    val result = resultIO.unsafeRunSync()
    result.isLeft shouldBe true
    result.left.value shouldBe a[NonFatalRateServiceError]
  }
}

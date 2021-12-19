package forex.services.rates.interpreters

import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.services.rates.Algebra
import org.mockito.Mockito.when
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

import java.time.OffsetDateTime

class OneFrameCacheSpec extends AnyFlatSpec with Matchers with EitherValues with MockitoSugar {
  "OneFrameCacheSpec" should "cache the result of get from delegating service" in {
    val mockedRateService = mock[Algebra[IO]]
    val targetPair = Rate.Pair(Currency.USD, Currency.JPY)
    when(mockedRateService.get(targetPair))
      .thenReturn(Right(Rate(targetPair, Price(0.1), Timestamp(OffsetDateTime.now()))).pure[IO])

    val oneFrameCache = OneFrameCache(mockedRateService)
    val result1 = oneFrameCache.get(targetPair).unsafeRunSync()
    val result2 = oneFrameCache.get(targetPair).unsafeRunSync()
    result1.isRight shouldBe true
    result1 shouldBe result2
  }

  "OneFrameCacheSpec" should "cache the result of getRates from delegating service" in {
    val mockedRateService = mock[Algebra[IO]]
    val targetPair1 = Rate.Pair(Currency.USD, Currency.JPY)
    val targetPair2 = Rate.Pair(Currency.JPY, Currency.USD)
    val targetPairs = Seq(targetPair1, targetPair2)
    val expectedResult1 = Right(Seq(
      Rate(targetPair1, Price(0.1), Timestamp(OffsetDateTime.parse("2021-12-19T00:00:00.000Z"))),
      Rate(targetPair2, Price(12f), Timestamp(OffsetDateTime.parse("2021-12-19T00:00:00.000Z")))
    ))
    val expectedResult2 = Right(
      Rate(targetPair2, Price(12f), Timestamp(OffsetDateTime.parse("2021-12-19T00:00:00.000Z")))
    )

    when(mockedRateService.getRates(targetPairs))
      .thenReturn(expectedResult1.pure[IO])

    val oneFrameCache = OneFrameCache(mockedRateService)
    val result1 = oneFrameCache.getRates(targetPairs).unsafeRunSync()
    val result2 = oneFrameCache.get(targetPair2).unsafeRunSync()
    result1 shouldBe expectedResult1
    result2 shouldBe expectedResult2
  }
}

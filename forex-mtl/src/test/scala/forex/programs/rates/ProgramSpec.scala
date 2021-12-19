package forex.programs.rates

import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.programs.rates.Protocol.GetRatesRequest
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import forex.services.RatesService
import org.mockito.Mockito.when

import java.time.OffsetDateTime

class ProgramSpec extends AnyFlatSpec with Matchers with EitherValues with MockitoSugar {
  "Program" should "get specific rate  correctly" in {
    val mockRateService = mock[RatesService[IO]]

    val allPairs = for {
      c1 <- Currency.allCurrency()
      c2 <- Currency.allCurrency() if c2 != c1
    } yield {
      Rate.Pair(c1, c2)
    }

    when(mockRateService.getRates(allPairs))
      .thenReturn(Right(allPairs.map(pair => Rate(pair, Price(0.1), Timestamp(OffsetDateTime.now())))).pure[IO])

    val result = Program(mockRateService).get(GetRatesRequest(Currency.USD, Currency.JPY)).unsafeRunSync()
    result.isRight shouldBe true
    result.value.pair shouldBe Rate.Pair(Currency.USD, Currency.JPY)
    result.value.price shouldBe Price(0.1)
  }
}

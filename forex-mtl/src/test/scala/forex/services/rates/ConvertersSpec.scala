package forex.services.rates

import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.services.rates.Protocol.OneFrameRate
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import forex.services.rates.Converters._

import java.time.OffsetDateTime

class ConvertersSpec extends AnyFlatSpec with Matchers {
  private val testTimestamp = OffsetDateTime.now()

  private val oneFrameRate = OneFrameRate(
    "USD",
    "JPY",
    0.11,
    0.22,
    0.33,
    testTimestamp
  )

  private val rate = Rate(
    Rate.Pair(Currency.USD, Currency.JPY),
    Price(0.33),
    Timestamp(testTimestamp)
  )

  "Converters" should "converter OneFrameRate to Rate correctly" in {
    val convertedRate = oneFrameRate.asRate

    convertedRate shouldBe rate
  }
}

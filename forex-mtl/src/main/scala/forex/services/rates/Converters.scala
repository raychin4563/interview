package forex.services.rates

import forex.domain.{Currency, Price, Rate, Timestamp}

object Converters {

  import Protocol._

  private[rates] implicit class OneFrameRateOps(val oneFrameRate: OneFrameRate) extends AnyVal {
    def asRate: Rate =
      Rate(
        Rate.Pair(Currency.fromString(oneFrameRate.from), Currency.fromString(oneFrameRate.to)),
        Price(oneFrameRate.price),
        Timestamp(oneFrameRate.time_stamp)
      )
  }
}

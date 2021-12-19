package forex.services.rates

import java.time.OffsetDateTime

object Protocol {

  type OneFrameRateResponse = Seq[OneFrameRate]

  final case class OneFrameRate(
    from: String,
    to: String,
    bid: Double,
    ask: Double,
    price: Double,
    time_stamp: OffsetDateTime
  )

  final case class OneFrameErrorResponse(
    error: String
  )
}
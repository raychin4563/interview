package forex.services.rates

import io.circe.Decoder
import io.circe.generic.AutoDerivation
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

import java.time.OffsetDateTime

object Protocol extends AutoDerivation {
  private implicit final val customConfig: Configuration = Configuration.default.withDefaults

  type OneFrameRateResponse = Seq[OneFrameRate]

  final case class OneFrameRate(
    from: String,
    to: String,
    bid: Double,
    ask: Double,
    price: Double,
    time_stamp: OffsetDateTime
  )

  object OneFrameRate {
    implicit val oneFrameRateDecoder: Decoder[OneFrameRate] = deriveConfiguredDecoder
  }

  final case class OneFrameErrorResponse(
    error: String
  )

  object OneFrameErrorResponse {
    implicit val oneFrameErrorResponseDecoder: Decoder[OneFrameErrorResponse] = deriveConfiguredDecoder
  }
}
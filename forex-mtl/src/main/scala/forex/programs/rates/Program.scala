package forex.programs.rates

import cats.Monad
import cats.data.EitherT
import cats.implicits.{catsSyntaxApplicativeId, toBifunctorOps}
import forex.domain._
import forex.programs.rates.errors.Error.RateLookupFailed
import forex.programs.rates.errors.{toProgramError, _}
import forex.services.RatesService

class Program[F[_]: Monad](
    ratesService: RatesService[F]
) extends Algebra[F] {

  private def allPairs(): Seq[Rate.Pair] = {
    val allCurrency = Currency.allCurrency()
    for {
      c1 <- allCurrency
      c2 <- allCurrency if c2 != c1
    } yield Rate.Pair(c1, c2)
  }

  override def get(request: Protocol.GetRatesRequest): F[Error Either Rate] = {
    val targetPair = Rate.Pair(request.from, request.to)
    val rateT = for {
      rates <- EitherT(ratesService.getRates(allPairs())).leftMap(toProgramError(_))
      rate <- EitherT
        .fromOptionF(
          rates.find(rate => rate.pair == targetPair).pure[F],
          RateLookupFailed(targetPair.toString + " not found")
        )
        .leftWiden[Error]
    } yield {
      rate
    }
    rateT.value
  }

}

object Program {

  def apply[F[_]: Monad](
      ratesService: RatesService[F]
  ): Algebra[F] = new Program[F](ratesService)

}

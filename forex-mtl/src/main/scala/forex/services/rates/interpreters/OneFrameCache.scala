package forex.services.rates.interpreters

import cats.data.EitherT
import cats.effect.Sync
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxEitherId, toFunctorOps, toTraverseOps}
import com.github.blemale.scaffeine.{Cache, Scaffeine}
import forex.domain.Rate
import forex.services.rates.{Algebra, errors}

import scala.concurrent.duration.DurationInt

class OneFrameCache[F[_]: Sync](cache: Cache[Rate.Pair, Rate], delegate: Algebra[F]) extends Algebra[F]{

  override def get(pair: Rate.Pair): F[Either[errors.Error, Rate]] = cache.getIfPresent(pair) match {
    case Some(rate) => rate.asRight[errors.Error].pure[F]
    case None => EitherT(delegate.get(pair))
      .semiflatTap(rate => cache.put(pair, rate).pure[F])
      .value
  }

  override def getRates(pairs: Seq[Rate.Pair]): F[Either[errors.Error, Seq[Rate]]] = {
    pairs.toVector.traverse(cache.getIfPresent) match {
      case Some(rates) => rates.asRight[errors.Error].widen[Seq[Rate]].pure[F]
      case None => EitherT(delegate.getRates(pairs))
        .semiflatTap(rates => cache.putAll(rates.map(rate => (rate.pair, rate)).toMap).pure[F])
        .value
    }
  }
}

object OneFrameCache {
  def apply[F[_]: Sync](realService: Algebra[F]): OneFrameCache[F] = {
    val cache: Cache[Rate.Pair, Rate] = Scaffeine()
      .expireAfterWrite(5.minute)
      .build()
    new OneFrameCache[F](cache, realService)
  }
}

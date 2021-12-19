package forex.services.rates.interpreters

import cats.Applicative
import cats.implicits.{toFunctorOps, toTraverseOps}
import cats.syntax.applicative._
import cats.syntax.either._
import forex.domain.{Price, Rate, Timestamp}
import forex.services.rates.Algebra
import forex.services.rates.errors._

class OneFrameDummy[F[_]: Applicative] extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] =
    Rate(pair, Price(BigDecimal(100)), Timestamp.now).asRight[Error].pure[F]

  override def getRates(pairs: Seq[Rate.Pair]): F[Either[Error, Seq[Rate]]] =
    pairs.toVector
      .traverse(p =>
        Rate(p, Price(BigDecimal(100)), Timestamp.now).asRight[Error]
      )
      .widen[Seq[Rate]]
      .pure[F]
}

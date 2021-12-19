package forex.services.rates

import cats.Applicative
import cats.effect.Sync
import forex.config.ApplicationConfig
import forex.services.rates.interpreters._
import org.http4s.client.Client

object Interpreters {
  def dummy[F[_]: Applicative]: Algebra[F] = new OneFrameDummy[F]()

  def concrete[F[_]: Sync](config: ApplicationConfig, client: Client[F]): Algebra[F] =
    OneFrameInterpreter[F](config, client)
}

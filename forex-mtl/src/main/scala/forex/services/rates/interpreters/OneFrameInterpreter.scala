package forex.services.rates.interpreters

import cats.data.EitherT
import cats.effect.Sync
import cats.implicits._
import forex.config.{ApplicationConfig, OneFrameConfig}
import forex.domain.Rate
import forex.services.rates.Converters.OneFrameRateOps
import forex.services.rates.Protocol.{OneFrameErrorResponse, OneFrameRateResponse}
import forex.services.rates.errors.Error.{NonFatalRateServiceError, OneFrameLookupFailed, ResponseDecodeFailure}
import forex.services.rates.{Algebra, errors}
import org.http4s.Uri.Scheme.http
import org.http4s.Uri.{Authority, RegName}
import org.http4s._
import org.http4s.circe.jsonOf
import org.http4s.client.Client

class OneFrameInterpreter[F[_]: Sync](config: OneFrameConfig, client: Client[F]) extends Algebra[F]  {

  implicit val oneFrameResponseEntityDecoder: EntityDecoder[F, OneFrameRateResponse] = jsonOf[F, OneFrameRateResponse]

  implicit val oneFrameErrorResponseEntityDecoder: EntityDecoder[F, OneFrameErrorResponse] =
    jsonOf[F, OneFrameErrorResponse]

  private def convertDecodeFailure(decodeFailure: DecodeFailure): Either[errors.Error, OneFrameRateResponse] =
    ResponseDecodeFailure(decodeFailure.getMessage())
      .asLeft[OneFrameRateResponse]
      .leftWiden[errors.Error]

  private def handleErrorResponse(
    response: Response[F],
    originError: InvalidMessageBodyFailure
  ): F[Either[errors.Error, OneFrameRateResponse]] = {
    response.attemptAs[OneFrameErrorResponse]
      .value
      .map {
        case Right(oneFrameResponse) => OneFrameLookupFailed(oneFrameResponse.error)
          .asLeft[OneFrameRateResponse]
          .leftWiden[errors.Error]
        case Left(_) => convertDecodeFailure(originError)
      }
  }

  private def sendRequest(request: Request[F]): F[Either[errors.Error, OneFrameRateResponse]] = {
    client.run(request)
      .use {
        case Status.Successful(response) =>
          response.attemptAs[OneFrameRateResponse]
            .value
            .flatMap {
              case Left(imbf: InvalidMessageBodyFailure) => handleErrorResponse(response, imbf)
              case Left(decodeFailure) => convertDecodeFailure(decodeFailure).pure[F]
              case Right(oneFrameRateResponse: OneFrameRateResponse) => oneFrameRateResponse.asRight[errors.Error].pure[F]
            }
        case response: Response[F] => NonFatalRateServiceError("Unexpected response: " + response.toString)
          .asLeft[OneFrameRateResponse]
          .leftWiden[errors.Error]
          .pure[F]
      }
  }

  override def get(pair: Rate.Pair): F[Either[errors.Error, Rate]] = {
    val request = Request[F](
      Method.GET,
      Uri(
        Some(http),
        Some(Authority(None, RegName(config.host), Some(config.port))),
        config.api.getRatePath,
        Query(("pair", Some(pair.from.toString + pair.to.toString))),
        None
      ),
      headers = Headers.of(Header("token", config.token))
    )
    val rateQuery = sendRequest(request)
    val oneFrameRateT = for {
      oneFrameRateResponse <- EitherT(rateQuery)
      oneFrameRate <- EitherT(
        oneFrameRateResponse.headOption
          .toRight[errors.Error](OneFrameLookupFailed("Response doesn't contain correct number of rate data"))
          .pure[F]
      )
    } yield {
      oneFrameRate.asRate
    }
    oneFrameRateT.value
  }
}

object OneFrameInterpreter {
  def apply[F[_]: Sync](config: ApplicationConfig, client: Client[F]): Algebra[F] =
    new OneFrameInterpreter[F](config.oneFrame, client)
}
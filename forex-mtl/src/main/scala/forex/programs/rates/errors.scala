package forex.programs.rates

import forex.services.rates.errors.{Error => RatesServiceError}

object errors {

  sealed trait Error extends Exception {
    override def getMessage: String = this.getMsg
    def getMsg: String
  }
  object Error {
    final case class RateLookupFailed(msg: String) extends Error {
      override def getMsg: String = msg
    }
    final case class DecodeError(msg: String) extends Error {
      override def getMsg: String = msg
    }
    final case class UnknownError(msg: String) extends Error {
      override def getMsg: String = msg
    }
  }

  def toProgramError(error: RatesServiceError): Error = {
    error match {
      case RatesServiceError.OneFrameLookupFailed(msg) => Error.RateLookupFailed(msg)
      case RatesServiceError.ResponseDecodeFailure(msg) => Error.DecodeError(msg)
      case RatesServiceError.NonFatalRateServiceError(msg) => Error.UnknownError(msg)
    }
  }
}

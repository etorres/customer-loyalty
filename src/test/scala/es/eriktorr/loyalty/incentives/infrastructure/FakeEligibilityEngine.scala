package es.eriktorr.loyalty.incentives.infrastructure

import cats.effect._
import es.eriktorr.loyalty.core.domain._
import es.eriktorr.loyalty.incentives.domain._

final class FakeEligibilityEngine extends EligibilityEngine[IO] {
  override def isEligible(userId: UserId, promotionalOffer: PromotionalOffer): IO[Boolean] =
    IO.pure(false)
}

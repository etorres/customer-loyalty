package es.eriktorr.loyalty.incentives.infrastructure

import cats.effect._
import cats.effect.concurrent.Ref
import es.eriktorr.loyalty.core.domain._
import es.eriktorr.loyalty.incentives.domain._

final class FakeEligibilityEngine(val ref: Ref[IO, Map[UserId, List[PromotionalOffer]]])
    extends EligibilityEngine[IO] {
  override def isEligible(userId: UserId, promotionalOffer: PromotionalOffer): IO[Boolean] =
    for {
      promotionalOffers <- ref.get
      isEligible = promotionalOffers.getOrElse(userId, List.empty).contains(promotionalOffer)
    } yield isEligible
}

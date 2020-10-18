package es.eriktorr.loyalty.incentives.infrastructure

import cats._
import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import es.eriktorr.loyalty.core.domain._
import es.eriktorr.loyalty.incentives.domain._

final class FakeEligibilityEngine(val ref: Ref[IO, Map[UserId, List[PromotionalOffer]]])
    extends EligibilityEngine[IO] {
  implicit val eqPromotionalOffer: Eq[PromotionalOffer] = Eq.fromUniversalEquals

  override def isEligible(userId: UserId, promotionalOffer: PromotionalOffer): IO[Boolean] =
    for {
      promotionalOffers <- ref.get
      isEligible = promotionalOffers.getOrElse(userId, List.empty).contains_(promotionalOffer)
    } yield isEligible
}

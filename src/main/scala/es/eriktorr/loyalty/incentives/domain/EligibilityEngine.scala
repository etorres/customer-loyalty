package es.eriktorr.loyalty.incentives.domain

import es.eriktorr.loyalty.core.domain._

trait EligibilityEngine[F[_]] {
  def isEligible(userId: UserId, promotionalOffer: PromotionalOffer): F[Boolean]
}

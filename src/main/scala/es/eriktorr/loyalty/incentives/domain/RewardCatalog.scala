package es.eriktorr.loyalty.incentives.domain

import es.eriktorr.loyalty.core.domain._

trait RewardCatalog[F[_]] {
  def rewardCampaignsFor(itemIds: ItemIds): F[RewardCampaigns]
}

package es.eriktorr.loyalty.incentives.infrastructure

import cats.effect._
import es.eriktorr.loyalty.core.domain._
import es.eriktorr.loyalty.incentives.domain._

final class FakeRewardCatalog extends RewardCatalog[IO] {
  override def rewardCampaignsFor(itemIds: ItemIds): IO[RewardCampaigns] = IO.pure(Map.empty)
}

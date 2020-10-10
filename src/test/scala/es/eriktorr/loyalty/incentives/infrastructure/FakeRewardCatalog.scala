package es.eriktorr.loyalty.incentives.infrastructure

import cats.effect._
import cats.effect.concurrent.Ref
import es.eriktorr.loyalty.core.domain._
import es.eriktorr.loyalty.incentives.domain._

final class FakeRewardCatalog(val ref: Ref[IO, RewardCampaigns]) extends RewardCatalog[IO] {
  override def rewardCampaignsFor(itemIds: ItemIds): IO[RewardCampaigns] =
    for {
      rewardCampaigns <- ref.get
      rewardCampaignsForItemIds = rewardCampaigns.filter {
        case (itemId, _) => itemIds.contains(itemId)
      }
    } yield rewardCampaignsForItemIds
}

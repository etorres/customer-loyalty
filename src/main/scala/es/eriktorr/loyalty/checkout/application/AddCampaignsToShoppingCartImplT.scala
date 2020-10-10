package es.eriktorr.loyalty.checkout.application

import cats._
import cats.data._
import cats.implicits._
import es.eriktorr.loyalty.checkout.domain.{ShoppingCartRepository, ShoppingCartWithPromotions}
import es.eriktorr.loyalty.core.domain.UserId
import es.eriktorr.loyalty.incentives.domain.{EligibilityEngine, RewardCatalog}

trait AddCampaignsToShoppingCartImplT {
  def implT[F[_]: Monad](
    eligibilityEngine: EligibilityEngine[F],
    rewardCatalog: RewardCatalog[F],
    shoppingCartRepository: ShoppingCartRepository[F]
  ): AddCampaignsToShoppingCart[F] =
    (userId: UserId) =>
      (for {
        shoppingCart <- OptionT(shoppingCartRepository.shoppingCartFor(userId))
        rewardCampaigns <- OptionT.liftF(
          rewardCatalog.rewardCampaignsFor(shoppingCart.shoppingCartItems.keySet.toList)
        )
        userEligibleRewards <- OptionT.liftF(
          rewardCampaigns.values.flatten.toList.distinct
            .map(x => eligibilityEngine.isEligible(userId, x).map(Option.when(_)(x)))
            .sequence
            .map(_.flatten)
        )
        eligibleCampaigns = rewardCampaigns.map {
          case (item, rewards) => (item, rewards.intersect(userEligibleRewards))
        }
        shoppingCartWithPromotions = {
          val itemsWithPromotions = shoppingCart.shoppingCartItems.map {
            case (itemId, quantity) =>
              (
                itemId,
                (quantity, eligibleCampaigns.getOrElse(itemId, List.empty))
              )
          }
          ShoppingCartWithPromotions(
            shoppingCart.shoppingCartId,
            shoppingCart.userId,
            itemsWithPromotions
          )
        }
      } yield shoppingCartWithPromotions).value
}

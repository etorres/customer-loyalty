package es.eriktorr.loyalty.checkout.application

import cats._
import cats.implicits._
import es.eriktorr.loyalty.checkout.domain.{ShoppingCartRepository, ShoppingCartWithPromotions}
import es.eriktorr.loyalty.core.domain.NewTypeInstances._
import es.eriktorr.loyalty.core.domain.{PromotionalOffer, UserId}
import es.eriktorr.loyalty.incentives.domain.{EligibilityEngine, RewardCatalog}

trait AddCampaignsToShoppingCartImpl {
  def impl[F[_]: Monad](
    eligibilityEngine: EligibilityEngine[F],
    rewardCatalog: RewardCatalog[F],
    shoppingCartRepository: ShoppingCartRepository[F]
  ): AddCampaignsToShoppingCart[F] =
    (userId: UserId) => {
      val F = Monad[F]
      for {
        maybeShoppingCart <- shoppingCartRepository.shoppingCartFor(userId)
        shoppingCartWithPromotions <- maybeShoppingCart.fold(
          F.pure(none[ShoppingCartWithPromotions])
        ) { shoppingCart =>
          for {
            rewardCampaigns <- rewardCatalog.rewardCampaignsFor(
              shoppingCart.shoppingCartItems.keys.toList
            )
            eligibleCampaigns <- rewardCampaigns
              .map {
                case (itemId, promotionalOffers) =>
                  F.pure(itemId)
                    .product(
                      promotionalOffers.traverse(promotionalOffer =>
                        F.ifA(eligibilityEngine.isEligible(userId, promotionalOffer))(
                          ifTrue = F.pure(promotionalOffer.some),
                          ifFalse = F.pure(none[PromotionalOffer])
                        )
                      )
                    )
              }
              .toList
              .traverse(identity)
            itemsWithPromotions = shoppingCart.shoppingCartItems.map {
              case (itemId, quantity) =>
                (
                  itemId,
                  (
                    quantity,
                    eligibleCampaigns
                      .find { case (x, _) => x === itemId }
                      .fold(List.empty[PromotionalOffer])(_._2.flatten)
                  )
                )
            }
            shoppingCartWithPromotions = ShoppingCartWithPromotions(
              shoppingCart.shoppingCartId,
              shoppingCart.userId,
              itemsWithPromotions
            ).some
          } yield shoppingCartWithPromotions
        }
      } yield shoppingCartWithPromotions
    }
}

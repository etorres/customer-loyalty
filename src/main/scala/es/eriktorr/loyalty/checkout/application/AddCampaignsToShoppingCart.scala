package es.eriktorr.loyalty.checkout.application

import es.eriktorr.loyalty.checkout.domain.ShoppingCartWithPromotions
import es.eriktorr.loyalty.core.domain.UserId

trait AddCampaignsToShoppingCart[F[_]] {
  def shoppingCartWithPromotionsFor(userId: UserId): F[Option[ShoppingCartWithPromotions]]
}

object AddCampaignsToShoppingCart extends AddCampaignsToShoppingCartImpl

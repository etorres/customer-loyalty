package es.eriktorr.loyalty.checkout.domain

import es.eriktorr.loyalty.core.domain.UserId

final case class ShoppingCartWithPromotions(
  shoppingCartId: ShoppingCartId,
  userId: Option[UserId],
  shoppingCartItems: ShoppingCartItemsWithPromotions
)

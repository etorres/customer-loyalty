package es.eriktorr.loyalty.checkout.domain

import es.eriktorr.loyalty.core.domain._

final case class ShoppingCart(
  shoppingCartId: ShoppingCartId,
  userId: Option[UserId],
  shoppingCartItems: ShoppingCartItems
)

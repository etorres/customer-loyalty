package es.eriktorr.loyalty.checkout.domain

import es.eriktorr.loyalty.core.domain._

trait ShoppingCartRepository[F[_]] {
  def shoppingCartFor(userId: UserId): F[Option[ShoppingCart]]
}

package es.eriktorr.loyalty.checkout.infrastructure

import cats.effect._
import cats.effect.concurrent.Ref
import es.eriktorr.loyalty.checkout.domain._
import es.eriktorr.loyalty.core.domain.UserId

final class FakeShoppingCartRepository(val ref: Ref[IO, Map[UserId, ShoppingCart]])
    extends ShoppingCartRepository[IO] {
  override def shoppingCartFor(userId: UserId): IO[Option[ShoppingCart]] =
    for {
      shoppingCarts <- ref.get
      shoppingCartForUserId = shoppingCarts.get(userId)
    } yield shoppingCartForUserId
}

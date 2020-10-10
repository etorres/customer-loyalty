package es.eriktorr.loyalty.checkout.infrastructure

import cats.effect._
import cats.implicits._
import es.eriktorr.loyalty.checkout.domain._
import es.eriktorr.loyalty.core.domain

final class FakeShoppingCartRepository extends ShoppingCartRepository[IO] {
  override def shoppingCartFor(userId: domain.UserId): IO[Option[ShoppingCart]] =
    IO.pure(none[ShoppingCart])
}

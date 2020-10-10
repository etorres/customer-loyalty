package es.eriktorr.loyalty.checkout.infrastructure

import cats._
import cats.implicits._
import es.eriktorr.loyalty.checkout.domain.ShoppingCartId
import es.eriktorr.loyalty.core.domain.{ItemId, SomeQuantity}
import eu.timepit.refined.api.Refined.unsafeApply
import org.scalacheck.Gen
import org.scalacheck.cats.implicits._

object CheckoutGenerators {
  val shoppingCartIdGen: Gen[ShoppingCartId] =
    Gen.uuid.map(x => ShoppingCartId(unsafeApply(x.show)))

  def shoppingCartItemGen(
    itemIdGen: Gen[ItemId],
    quantityGen: Gen[SomeQuantity]
  ): Gen[(ItemId, SomeQuantity)] =
    Applicative[Gen].product(itemIdGen, quantityGen)
}

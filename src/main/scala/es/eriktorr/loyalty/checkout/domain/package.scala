package es.eriktorr.loyalty.checkout

import es.eriktorr.loyalty.core.domain._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string._
import io.estatico.newtype.macros.newtype

package object domain {
  type ShoppingCartItems = Map[ItemId, SomeQuantity]
  type ShoppingCartItemsWithPromotions = Map[ItemId, (SomeQuantity, List[PromotionalOffer])]

  @newtype case class ShoppingCartId(toUuid: String Refined Uuid)
}

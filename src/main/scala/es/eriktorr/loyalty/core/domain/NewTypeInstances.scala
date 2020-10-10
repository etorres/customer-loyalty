package es.eriktorr.loyalty.core.domain

import cats._
import cats.kernel.Eq

object NewTypeInstances {
  implicit val eqItemId: Eq[ItemId] = Eq.fromUniversalEquals

  implicit val showItemId: Show[ItemId] = Show.show(_.toString)

  implicit val showSomeQuantity: Show[SomeQuantity] = Show.show(_.toString)

  implicit val showUserId: Show[UserId] = Show.show(_.toString)
}

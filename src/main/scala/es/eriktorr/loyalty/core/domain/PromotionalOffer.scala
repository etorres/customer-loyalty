package es.eriktorr.loyalty.core.domain

import squants.Dimensionless
import squants.DimensionlessConversions._
import squants.market.Money

sealed trait MoreOfTheSame {
  val discountOnAdditionalItems: Dimensionless = 0.percent
}

sealed trait PromotionalOffer extends Product with Serializable with MoreOfTheSame

object PromotionalOffer {
  case object FreeDelivery extends PromotionalOffer

  final case class FreeSamples(itemIds: ItemIds) extends PromotionalOffer

  case object BuyOneGetOneFree extends PromotionalOffer {
    override val discountOnAdditionalItems: Dimensionless = 100.percent
  }

  case object BuyOneGetOneHalfPrice extends PromotionalOffer {
    override val discountOnAdditionalItems: Dimensionless = 50.percent
  }

  final case class VolumeDiscount(minimumSpend: Money, totalDiscount: Dimensionless)
      extends PromotionalOffer

  final case class GiftCard(value: Money, maximumRedeems: SomeQuantity) extends PromotionalOffer

  final case class LoyaltyPoints(amount: SomeQuantity) extends PromotionalOffer
}

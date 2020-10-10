package es.eriktorr.loyalty.core.domain

import squants.Dimensionless
import squants.DimensionlessConversions._
import squants.market.Money

sealed abstract class PromotionalOffer extends Product with Serializable

object PromotionalOffer {
  sealed trait MoreOfTheSame extends PromotionalOffer {
    val discountOnAdditionalItems: Dimensionless = 0.percent
  }

  case object FreeDelivery extends PromotionalOffer

  final case class FreeSamples(itemIds: ItemIds) extends PromotionalOffer

  case object BuyOneGetOneFree extends MoreOfTheSame {
    override val discountOnAdditionalItems: Dimensionless = 100.percent
  }

  case object BuyOneGetOneHalfPrice extends MoreOfTheSame {
    override val discountOnAdditionalItems: Dimensionless = 50.percent
  }

  final case class VolumeDiscount(minimumSpend: Money, totalDiscount: Dimensionless)
      extends PromotionalOffer

  final case class GiftCard(value: Money, maximumRedeems: SomeQuantity) extends PromotionalOffer

  final case class LoyaltyPoints(amount: SomeQuantity) extends PromotionalOffer
}

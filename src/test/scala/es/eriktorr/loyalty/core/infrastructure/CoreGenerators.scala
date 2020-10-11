package es.eriktorr.loyalty.core.infrastructure

import cats.implicits._
import es.eriktorr.loyalty.core.domain.PromotionalOffer._
import es.eriktorr.loyalty.core.domain.{ItemId, PromotionalOffer, SomeQuantity, UserId}
import eu.timepit.refined.api.Refined.unsafeApply
import eu.timepit.refined.auto._
import org.scalacheck.Gen
import squants.DimensionlessConversions._
import squants.market.MoneyConversions._

object CoreGenerators {
  val itemIdGen: Gen[ItemId] = Gen.uuid.map(x => ItemId(unsafeApply(x.show)))

  def itemWithPromotionGen(
    itemId: ItemId,
    quantity: SomeQuantity,
    promotionalOffers: List[PromotionalOffer]
  ): Gen[(ItemId, (SomeQuantity, List[PromotionalOffer]))] =
    for {
      promotionalOffers <- Gen.nonEmptyContainerOf[Set, PromotionalOffer](
        Gen.oneOf(promotionalOffers)
      )
    } yield (itemId, (quantity, promotionalOffers.toList))

  def itemMaybeWithPromotionGen(
    itemId: ItemId,
    quantity: SomeQuantity,
    promotionalOffers: List[PromotionalOffer]
  ): Gen[(ItemId, (SomeQuantity, List[PromotionalOffer]))] =
    for {
      promotionalOffers <- Gen.containerOf[Set, PromotionalOffer](Gen.oneOf(promotionalOffers))
    } yield (itemId, (quantity, promotionalOffers.toList))

  val promotionOffersGen: Gen[List[PromotionalOffer]] = Gen.const(
    List(
      BuyOneGetOneFree,
      BuyOneGetOneHalfPrice,
      FreeDelivery,
      FreeSamples(List(ItemId("dea0618e-0a67-11eb-adc1-0242ac120002"))),
      GiftCard(10.0.EUR, SomeQuantity(1)),
      LoyaltyPoints(SomeQuantity(100)),
      VolumeDiscount(20.0.EUR, 10.percent)
    )
  )

  val quantityGen: Gen[SomeQuantity] = Gen.chooseNum(1, 10).map(x => SomeQuantity(unsafeApply(x)))

  val userIdGen: Gen[UserId] = Gen.identifier.map(userId => UserId(unsafeApply(userId)))
}

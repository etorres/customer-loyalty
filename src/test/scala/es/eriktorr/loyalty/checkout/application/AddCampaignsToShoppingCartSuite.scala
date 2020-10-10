package es.eriktorr.loyalty.checkout.application

import cats._
import cats.effect._
import cats.implicits._
import es.eriktorr.loyalty.checkout.domain.{ShoppingCartId, ShoppingCartWithPromotions}
import es.eriktorr.loyalty.checkout.infrastructure.FakeShoppingCartRepository
import es.eriktorr.loyalty.core.domain.{ItemId, PromotionalOffer, SomeQuantity, UserId}
import es.eriktorr.loyalty.core.domain.PromotionalOffer._
import es.eriktorr.loyalty.effect._
import es.eriktorr.loyalty.incentives.infrastructure.{FakeEligibilityEngine, FakeRewardCatalog}
import eu.timepit.refined.api.Refined.unsafeApply
import eu.timepit.refined.auto._
import org.scalacheck._
import org.scalacheck.cats.implicits._
import squants.DimensionlessConversions._
import squants.market.MoneyConversions._
import weaver._
import weaver.scalacheck._

object AddCampaignsToShoppingCartSuite extends IOSuite with IOCheckers {
  override type Res = AddCampaignsToShoppingCart[IO]

  override def sharedResource: Resource[IO, Res] =
    AddCampaignsToShoppingCart
      .impl[IO](new FakeEligibilityEngine, new FakeRewardCatalog, new FakeShoppingCartRepository)
      .toResource

  test("Add eligible campaigns to shopping cart") { addCampaignsToShoppingCart =>
    val userIdGen = Gen.identifier.map(userId => UserId(unsafeApply(userId)))

    val shoppingCartIdGen = Gen.uuid.map(x => ShoppingCartId(unsafeApply(x.show)))

    val itemIdGen = Gen.uuid.map(x => ItemId(unsafeApply(x.show)))

    val quantityGen = Gen.chooseNum(1, 10).map(x => SomeQuantity(unsafeApply(x)))

    val shoppingCartItemGen = Applicative[Gen].product(itemIdGen, quantityGen)

    val promotionGen: Gen[PromotionalOffer] =
      Gen.oneOf(
        BuyOneGetOneFree,
        BuyOneGetOneHalfPrice,
        FreeDelivery,
        FreeSamples(List(ItemId("dea0618e-0a67-11eb-adc1-0242ac120002"))),
        GiftCard(10.0.EUR, SomeQuantity(1)),
        LoyaltyPoints(SomeQuantity(100)),
        VolumeDiscount(20.0.EUR, 10.percent)
      )

    def itemWithPromotionGen(itemId: ItemId, quantity: SomeQuantity) =
      for {
        promotionalOffers <- Gen.nonEmptyContainerOf[List, PromotionalOffer](promotionGen)
      } yield (itemId, (quantity, promotionalOffers))

    val gen = for {
      userId <- userIdGen
      shoppingCartId <- shoppingCartIdGen
      shoppingCartItems <- Gen.containerOfN[List, (ItemId, SomeQuantity)](10, shoppingCartItemGen)
      (itemsWithPromotion, itemsWithoutPromotion) = shoppingCartItems.splitAt(3)
      generatedShoppingCartItemsWithPromotion <- itemsWithPromotion.traverse {
        case (itemId, quantity) =>
          Gen
            .containerOf[List, (ItemId, (SomeQuantity, List[PromotionalOffer]))](
              itemWithPromotionGen(itemId, quantity)
            )
      }
      shoppingCartItemsWithPromotion = generatedShoppingCartItemsWithPromotion.flatten
      shoppingCartItemsWithoutPromotion = itemsWithoutPromotion.map {
        case (itemId, quantity) => (itemId, (quantity, List.empty[PromotionalOffer]))
      }
    } yield TestCase(
      userId,
      shoppingCartId,
      shoppingCartItemsWithPromotion,
      shoppingCartItemsWithPromotion ++ shoppingCartItemsWithoutPromotion
    )

    implicit val showItemId: Show[TestCase] = Show.show(_.toString)

    forall(gen) { testCase =>
      for {
        shoppingCartWithPromotions <- addCampaignsToShoppingCart.shoppingCartWithPromotionsFor(
          testCase.userId
        )
      } yield expect(
        shoppingCartWithPromotions == ShoppingCartWithPromotions(
          testCase.shoppingCartId,
          testCase.userId.some,
          testCase.shoppingCartItemsWithPromotion.toMap
        ).some
      )
    }
  }
}

final case class TestCase(
  userId: UserId,
  shoppingCartId: ShoppingCartId,
  shoppingCartItemsWithPromotion: List[(ItemId, (SomeQuantity, List[PromotionalOffer]))],
  allShoppingCartItems: List[(ItemId, (SomeQuantity, List[PromotionalOffer]))]
)

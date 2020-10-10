package es.eriktorr.loyalty.checkout.application

import cats._
import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import es.eriktorr.loyalty.checkout.domain.{
  ShoppingCart,
  ShoppingCartId,
  ShoppingCartWithPromotions
}
import es.eriktorr.loyalty.checkout.infrastructure.FakeShoppingCartRepository
import es.eriktorr.loyalty.core.domain.{
  ItemId,
  PromotionalOffer,
  RewardCampaigns,
  SomeQuantity,
  UserId
}
import es.eriktorr.loyalty.core.domain.PromotionalOffer._
import es.eriktorr.loyalty.incentives.infrastructure.{FakeEligibilityEngine, FakeRewardCatalog}
import eu.timepit.refined.api.Refined.unsafeApply
import eu.timepit.refined.auto._
import org.scalacheck._
import org.scalacheck.cats.implicits._
import squants.DimensionlessConversions._
import squants.market.MoneyConversions._
import weaver._
import weaver.scalacheck._

object AddCampaignsToShoppingCartSuite extends SimpleIOSuite with IOCheckers {
  implicit val showItemId: Show[TestCase] = Show.show(_.toString)

  private[this] val userIdGen = Gen.identifier.map(userId => UserId(unsafeApply(userId)))

  private[this] val shoppingCartIdGen = Gen.uuid.map(x => ShoppingCartId(unsafeApply(x.show)))

  private[this] val itemIdGen = Gen.uuid.map(x => ItemId(unsafeApply(x.show)))

  private[this] val quantityGen = Gen.chooseNum(1, 10).map(x => SomeQuantity(unsafeApply(x)))

  private[this] val shoppingCartItemGen = Applicative[Gen].product(itemIdGen, quantityGen)

  private[this] val promotionGen: Gen[PromotionalOffer] =
    Gen.oneOf(
      BuyOneGetOneFree,
      BuyOneGetOneHalfPrice,
      FreeDelivery,
      FreeSamples(List(ItemId("dea0618e-0a67-11eb-adc1-0242ac120002"))),
      GiftCard(10.0.EUR, SomeQuantity(1)),
      LoyaltyPoints(SomeQuantity(100)),
      VolumeDiscount(20.0.EUR, 10.percent)
    )

  private[this] def itemWithPromotionGen(itemId: ItemId, quantity: SomeQuantity) =
    for {
      promotionalOffers <- Gen.nonEmptyContainerOf[List, PromotionalOffer](promotionGen)
    } yield (itemId, (quantity, promotionalOffers))

  private[this] def itemMaybeWithPromotionGen(itemId: ItemId, quantity: SomeQuantity) =
    for {
      promotionalOffers <- Gen.containerOf[List, PromotionalOffer](promotionGen)
    } yield (itemId, (quantity, promotionalOffers))

  private[this] val gen = for {
    userId <- userIdGen
    shoppingCartId <- shoppingCartIdGen
    shoppingCartItems <- Gen.containerOfN[List, (ItemId, SomeQuantity)](10, shoppingCartItemGen)
    (itemsWithPromotion, itemsMaybeWithPromotion) = shoppingCartItems.splitAt(3)
    shoppingCartItemsWithPromotion <- itemsWithPromotion.traverse {
      case (itemId, quantity) =>
        Gen
          .containerOf[List, (ItemId, (SomeQuantity, List[PromotionalOffer]))](
            itemWithPromotionGen(itemId, quantity)
          )
    }
    shoppingCartItemsMaybeWithPromotion <- itemsMaybeWithPromotion.traverse {
      case (itemId, quantity) =>
        Gen
          .containerOf[List, (ItemId, (SomeQuantity, List[PromotionalOffer]))](
            itemMaybeWithPromotionGen(itemId, quantity)
          )
    }
    eligibleShoppingCartItems = shoppingCartItemsWithPromotion.flatten
    notEligibleShoppingCartItems = shoppingCartItemsMaybeWithPromotion.flatten
  } yield TestCase(
    userId,
    shoppingCartId,
    eligibleShoppingCartItems,
    eligibleShoppingCartItems ++ notEligibleShoppingCartItems
  )

  simpleTest("Add eligible campaigns to shopping cart") {
    forall(gen) { testCase =>
      val addCampaignsToShoppingCart = AddCampaignsToShoppingCart
        .impl[IO](
          new FakeEligibilityEngine(
            Ref.unsafe[IO, Map[UserId, List[PromotionalOffer]]](
              Map(testCase.userId -> testCase.eligibleShoppingCartItems.flatMap {
                case (_, (_, promotionalOffers)) => promotionalOffers
              })
            )
          ),
          new FakeRewardCatalog(Ref.unsafe[IO, RewardCampaigns](testCase.allShoppingCartItems.map {
            case (itemId, (_, promotionalOffers)) => (itemId, promotionalOffers)
          }.toMap)),
          new FakeShoppingCartRepository(
            Ref.unsafe[IO, Map[UserId, ShoppingCart]](
              Map(
                testCase.userId -> ShoppingCart(
                  shoppingCartId = testCase.shoppingCartId,
                  userId = testCase.userId.some,
                  shoppingCartItems = testCase.allShoppingCartItems.map {
                    case (itemId, (quantity, _)) => (itemId, quantity)
                  }.toMap
                )
              )
            )
          )
        )
      for {
        shoppingCartWithPromotions <- addCampaignsToShoppingCart.shoppingCartWithPromotionsFor(
          testCase.userId
        )
      } yield expect(
        shoppingCartWithPromotions == ShoppingCartWithPromotions(
          testCase.shoppingCartId,
          testCase.userId.some,
          testCase.eligibleShoppingCartItems.toMap
        ).some
      )
    }
  }
}

final case class TestCase(
  userId: UserId,
  shoppingCartId: ShoppingCartId,
  eligibleShoppingCartItems: List[(ItemId, (SomeQuantity, List[PromotionalOffer]))],
  allShoppingCartItems: List[(ItemId, (SomeQuantity, List[PromotionalOffer]))]
)

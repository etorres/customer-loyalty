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
import es.eriktorr.loyalty.checkout.infrastructure.CheckoutGenerators.{
  shoppingCartIdGen,
  shoppingCartItemGen
}
import es.eriktorr.loyalty.checkout.infrastructure.FakeShoppingCartRepository
import es.eriktorr.loyalty.core.domain._
import es.eriktorr.loyalty.core.infrastructure.CoreGenerators._
import es.eriktorr.loyalty.incentives.infrastructure.{FakeEligibilityEngine, FakeRewardCatalog}
import org.scalacheck._
import org.scalacheck.cats.implicits._
import weaver._
import weaver.scalacheck._

import scala.util.Random

object AddCampaignsToShoppingCartSuite extends SimpleIOSuite with IOCheckers {
  implicit val showItemId: Show[TestCase] = Show.show(_.toString)

  private[this] val gen = for {
    userId <- userIdGen
    shoppingCartId <- shoppingCartIdGen
    shoppingCartItems <- Gen
      .containerOfN[List, (ItemId, SomeQuantity)](10, shoppingCartItemGen(itemIdGen, quantityGen))
    allPromotionOffers <- promotionOffersGen
    (eligiblePromotionOffers, notEligiblePromotionOffers) = Random
      .shuffle(allPromotionOffers)
      .splitAt(3)
    (itemsWithPromotion, itemsMaybeWithPromotion) = shoppingCartItems.splitAt(3)
    shoppingCartItemsWithEligiblePromotions <- itemsWithPromotion.traverse {
      case (itemId, quantity) =>
        Gen.containerOf[List, (ItemId, (SomeQuantity, List[PromotionalOffer]))](
          itemWithPromotionGen(itemId, quantity, eligiblePromotionOffers)
        )
    }
    shoppingCartItemsMaybeWithNotEligiblePromotions <- itemsMaybeWithPromotion.traverse {
      case (itemId, quantity) =>
        Gen.containerOf[List, (ItemId, (SomeQuantity, List[PromotionalOffer]))](
          itemMaybeWithPromotionGen(itemId, quantity, notEligiblePromotionOffers)
        )
    }
    eligibleShoppingCartItems = shoppingCartItemsWithEligiblePromotions.flatten
    notEligibleShoppingCartItems = shoppingCartItemsMaybeWithNotEligiblePromotions.flatten
  } yield TestCase(
    userId,
    shoppingCartId,
    eligibleShoppingCartItems,
    notEligibleShoppingCartItems,
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
          testCase.eligibleShoppingCartItems.toMap ++ testCase.notEligibleShoppingCartItems.map {
            case (itemId, (quantity, _)) => (itemId, (quantity, List.empty))
          }
        ).some
      )
    }
  }
}

final case class TestCase(
  userId: UserId,
  shoppingCartId: ShoppingCartId,
  eligibleShoppingCartItems: List[(ItemId, (SomeQuantity, List[PromotionalOffer]))],
  notEligibleShoppingCartItems: List[(ItemId, (SomeQuantity, List[PromotionalOffer]))],
  allShoppingCartItems: List[(ItemId, (SomeQuantity, List[PromotionalOffer]))]
)

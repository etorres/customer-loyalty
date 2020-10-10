package es.eriktorr.loyalty.core

import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric._
import eu.timepit.refined.string._
import io.estatico.newtype.macros.newtype

package object domain {
  type ItemIds = List[ItemId]

  type RewardCampaigns = Map[ItemId, List[PromotionalOffer]]

  @newtype case class ItemId(toText: String Refined Uuid)

  @newtype case class SomeQuantity(toInt: Int Refined Positive)

  @newtype case class UserId(
    toText: String Refined MatchesRegex[W.`"^([A-Za-z]+)([A-Za-z0-9_]{2,6})([A-Za-z0-9]+)$"`.T]
  )
}

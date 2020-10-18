package es.eriktorr.loyalty.shared.infrastructure

import org.scalacheck.Gen
import org.scalacheck.rng.Seed

object GeneratorSyntax {
  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
  implicit class GeneratorOps[A](self: Gen[A]) {
    def sampleWithSeed(name: String, seed: Seed = Seed.random()): A = {
      println(s"Seed in Base64 for $name: ${seed.toBase64}")
      self.apply(Gen.Parameters.default, seed).get
    }
  }
}

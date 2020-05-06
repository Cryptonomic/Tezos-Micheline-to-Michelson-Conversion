package tech.cryptonomic.tezos.translator

import Helpers._
import tech.cryptonomic.tezos.translator.michelson.MichelineToMichelson
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest._
import org.scalatest.matchers.should.Matchers
import tech.cryptonomic.tezos.translator.michelson.ast.Schema

class MichelineToMichelsonTranslatorSpec
    extends FlatSpec
    with TableDrivenPropertyChecks
    with Matchers {

  "JsonToMichelson" should "translate from Micheline to Michelson" in {
    forAll(Samples.translations) { (michelson: String, micheline: String) =>
      val translation = MichelineToMichelson.convert[Schema](micheline)
      translation.map(_.noSpaces) shouldEqual Right(michelson.noSpaces)
    }
  }

}

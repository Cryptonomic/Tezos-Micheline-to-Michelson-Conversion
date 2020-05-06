package tech.cryptonomic.tezos.translator.michelson.parser

import io.circe.Decoder

case class LambdaForm(code: List[JsonSection])

object LambdaForm {
  implicit val lambdaFormDecoder: Decoder[LambdaForm] = {
    import GenericDerivation._
    Decoder.instance(c => c.downArray.downField("code").as[List[JsonSection]].map(LambdaForm(_)))
  }
}

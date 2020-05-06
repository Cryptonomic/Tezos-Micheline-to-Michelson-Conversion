package tech.cryptonomic.tezos.translator.michelson

import tech.cryptonomic.tezos.translator.michelson.ast.Node
import tech.cryptonomic.tezos.translator.michelson.parser.Parser
import tech.cryptonomic.tezos.translator.michelson.emitter.MichelsonEmitter._

/* Converts Micheline JSON syntax into Michelson */
object MichelineToMichelson {

  def convert[T <: Node: Parser](json: String): Either[Throwable, String] =
    Parser.parse[T](json).map(_.emit)
}

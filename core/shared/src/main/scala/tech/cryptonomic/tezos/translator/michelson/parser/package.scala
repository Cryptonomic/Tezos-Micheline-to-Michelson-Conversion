package tech.cryptonomic.tezos.translator.michelson

package object parser {
  type EmbeddedElement =
    Either[Either[JsonExpression, JsonInstruction], List[JsonInstruction]]
}

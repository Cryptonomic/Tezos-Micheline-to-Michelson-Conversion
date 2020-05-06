package tech.cryptonomic.tezos.translator.michelson.parser

import tech.cryptonomic.tezos.translator.michelson.ast._

object EmbeddedElement {
  def toMichelsonElement(embeddedElement: EmbeddedElement): Node =
    embeddedElement match {
      case Left(Left(jsonExpression)) => jsonExpression.toMichelsonExpression
      case Left(Right(jsonInstruction)) =>
        Instruction.normalize(jsonInstruction.toMichelsonInstruction)
      case Right(Nil) => EmptyInstruction
      case Right(jsonInstructions) =>
        InstructionSequence(
          jsonInstructions.map(_.toMichelsonInstruction)
        )
    }
}

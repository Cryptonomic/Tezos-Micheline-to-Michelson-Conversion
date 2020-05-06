package tech.cryptonomic.tezos.translator.michelson.parser

import tech.cryptonomic.tezos.translator.michelson.ast._

import scala.collection.immutable.List

/*
 * Wrapper for json section
 *
 * {"code": [{"prim": "parameter", "args": [{...}]}, {"prim": "storage", "args": [{...}]}, {"prim": "code", "args": [[{...}]]}]}
 * |         |                              |        |                            |        |                          |
 * |         JsonTypeSection                JsonType JsonTypeSection              JsonType ExpressionSection          JsonInstruction
 * JsonDocument
 *
 * We can distinguish which JsonSection it is by looking at its args. JsonTypeSection contains a single sequence, since
 * JsonExpressionSection contains an embedded one.
 *
 * */
sealed trait JsonSection

case class JsonExpressionSection(prim: String, args: List[JsonExpression]) extends JsonSection {
  def toMichelsonExpression: Option[Expression] =
    args.headOption.map(_.toMichelsonExpression)
}

case class JsonCodeSection(
  prim: String,
  args: Either[List[List[JsonInstruction]], List[JsonInstruction]]
) extends JsonSection {
  def toMichelsonCode: Code =
    Code(
      args.map(List(_)).merge.flatten.map(_.toMichelsonInstruction)
    )
}

sealed trait JsonExpression {
  def toMichelsonExpression: Expression
}

/*
 * Wrapper for type
 *
 * {"prim": "pair", "args": [{"prim": "int"}, {"prim": "address"}]}
 * |                         |                |
 * |                         |                single type "address"
 * |                         single type "int"
 * type "pair" with two arguments
 *
 * {"prim": "pair", "args": [{"prim": "int"}, []]}
 * |                         |                |
 * |                         |                empty expression
 * |                         single type "int"
 * type "pair" with two arguments
 *
 * Empty expression is represented as an empty array in JSON.
 *
 * */
case class JsonType(prim: String, args: Option[List[EmbeddedElement]], annots: Option[List[String]] = None)
    extends JsonExpression {
  override def toMichelsonExpression: Expression =
    Type(
      prim,
      args.getOrElse(List.empty).map(EmbeddedElement.toMichelsonElement),
      annots.getOrElse(List.empty)
    )
}

/*
 * Wrapper for instruction
 *
 * [{"prim": "DIP", "args": [[{"prim": "DUP"}]]}, [{"prim": "DIP", "args": [[{"prim": "NIL", "args": [{"prim": "operation"}]}]]}]]
 *  |                         |                   ||                         |                        |
 *  JsonComplexInstruction    |                   |JsonComplexInstruction    JsonSimpleInstruction    JsonType
 *                            |                   |
 *                            |                   JsonInstructionSequence
 *                            JsonSimpleInstruction
 *
 * */
sealed trait JsonInstruction {
  def toMichelsonInstruction: Instruction
}

case class JsonSimpleInstruction(
  prim: String,
  args: Option[List[EmbeddedElement]] = None,
  annots: Option[List[String]] = None
) extends JsonInstruction {
  override def toMichelsonInstruction: Instruction =
    SingleInstruction(
      name = prim,
      annotations = annots.getOrElse(List.empty),
      embeddedElements = args.getOrElse(List.empty).map(EmbeddedElement.toMichelsonElement)
    )
}

case class JsonInstructionSequence(instructions: List[JsonInstruction]) extends JsonInstruction {
  override def toMichelsonInstruction: Instruction =
    InstructionSequence(instructions.map(_.toMichelsonInstruction))
}

/*
 * Wrapper for int constant
 *
 * {"int": "0"}
 *
 * */
case class JsonIntConstant(int: String) extends JsonInstruction {
  override def toMichelsonInstruction: Instruction = IntConstant(int)
}

/*
 * Wrapper for string constant
 *
 * {"string": "0"}
 *
 * */
case class JsonStringConstant(string: String) extends JsonInstruction {
  override def toMichelsonInstruction: Instruction = StringConstant(string)
}

/*
 * Wrapper for bytes constant
 *
 * {"bytes": "0500"}
 *
 * */
case class JsonBytesConstant(bytes: String) extends JsonInstruction {
  override def toMichelsonInstruction: Instruction = BytesConstant(bytes)
}


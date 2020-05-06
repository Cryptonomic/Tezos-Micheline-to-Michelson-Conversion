package tech.cryptonomic.tezos.translator.michelson.ast

sealed trait Node

sealed trait Instruction extends Node
sealed trait Expression  extends Node

/*
 * Class representing a type
 *
 * Examples:
 *
 *   unit
 *   |
 *   simple type represented as Type("prim")
 *
 *   (contract (or (option address) int))
 *    |        |    |               |
 *    |        |    |               single type "int"
 *    |        |    type "option" with one argument "address"
 *    |        type "or" with two arguments: "option address" and "int"
 *    type "contract" with one complex argument
 *
 *    The type above can be represented as below:
 *
 *    Type("contract", List(Type("or", List(Type("option", List(Type("address"))), Type("int")))))
 *
 *    (pair 0 {})
 *     |    | |
 *     |    |  EmptyExpression
 *     |    IntConstant
 *     Type "pair" with two arguments
 *
 *    The type above can be represented as below:
 *
 *    Type("pair", List(IntConstant(0), EmptyExpression))
 * */
case class Type(
  prim: String,
  args: List[Node] = List.empty,
  annotations: List[String] = List.empty
) extends Expression

/* Class representing an empty expression */
case object EmptyExpression extends Expression

/*
 * Class representing a Michelson instruction (code section).
 *
 * In fact, you can use this type to represent Data as well. According to the grammar, it should be a separate type for
 * Data but since Data type is represented in the same way as an Instruction, the code is simplified and treats Data as
 * Instruction.
 *
 * Example:
 *
 *   { DIP { DIP { DUP } ; NIL operation } ; SWAP ; {} }
 *   | |   | |     |       |                 |      |
 *   | |   | |     |       |                 |      EmptyInstruction
 *   | |   | |     |       |                 SingleInstruction (not typed)
 *   | |   | |     |       SingleInstruction (with type "operation")
 *   | |   | |     SingleInstruction (not typed)
 *   | |   | SingleInstruction (with embedded "DUP" instruction as a one element List)
 *   | |   InstructionSequence (containing a complex instruction "DIP { ... }" and a simple one "NIL operation" separated with ";")
 *   | SingleInstruction (with embedded InstructionSequence as above)
 *   InstructionSequence (with three instructions separated with ";": "DIP { ... }", "SWAP" and empty instruction)
 * */

/* Class representing a simple Michelson instruction which can contains following expressions */
case class SingleInstruction(
  name: String,
  embeddedElements: List[Node],
  annotations: List[String]
) extends Instruction

/* Class representing a sequence of Michelson instructions */
case class InstructionSequence(instructions: List[Instruction]) extends Instruction

/* Class representing an int constant */
case class IntConstant(int: String) extends Instruction

/* Class representing a string constant */
case class StringConstant(string: String) extends Instruction

/* Class representing a bytes constant */
case class BytesConstant(bytes: String) extends Instruction

/* Class representing an empty Michelson instruction */
case object EmptyInstruction extends Instruction

object Instruction {
  def normalize(i: Instruction): Instruction = i match {
    case i: InstructionSequence => if (i.instructions.isEmpty) EmptyInstruction else i
    case other                  => other
  }
}

case class Code(instructions: List[Instruction])

/* Class representing a whole Michelson schema */
case class Schema(parameter: Expression, storage: Expression, code: Code) extends Node

object Schema {
  lazy val empty: Schema =
    Schema(EmptyExpression, EmptyExpression, Code(List.empty))
}

object SingleInstruction {
  def simple(name: String): SingleInstruction = SingleInstruction(name, List.empty, List.empty)
}

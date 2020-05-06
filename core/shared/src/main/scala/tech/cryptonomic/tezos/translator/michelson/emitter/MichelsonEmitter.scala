package tech.cryptonomic.tezos.translator.michelson.emitter

import tech.cryptonomic.tezos.translator.michelson.ast._

/* Implicitly adds emit methods for Michelson AST */
object MichelsonEmitter {

  implicit class MichelsonElementRenderer(val self: Node) extends EscapeJson {
    def emit: String = self match {

      // instructions
      case SingleInstruction(
          name,
          List(sequence1: InstructionSequence, sequence2: InstructionSequence),
          _
          ) =>
        val indent         = " " * (name.length + 1)
        val embeddedIndent = indent + " " * 2

        s"""$name { ${emitInstructions(embeddedIndent)(sequence1.instructions)} }
           |$indent{ ${emitInstructions(embeddedIndent)(sequence2.instructions)} }""".stripMargin

      case SingleInstruction(name, Nil, Nil) => name
      case SingleInstruction(name, args, annotations) =>
        s"$name ${(annotations ++ args.map(_.emit)).mkString(" ")}"
      case InstructionSequence(args) => s"{ ${args.map(_.emit).mkString(" ; ")} }"
      case EmptyInstruction          => "{}"

      // expressions
      case Type(name, Nil, Nil)          => name
      case Type(name, args, annotations) => s"($name ${(annotations ++ args.map(_.emit)).mkString(" ")})"
      case IntConstant(constant)         => constant
      case StringConstant(constant)      => escapeJsonString(constant)
      case BytesConstant(constant)       => s"0x$constant"
      case EmptyExpression               => "{}"

      // schema
      case Schema(EmptyExpression, EmptyExpression, Code(Nil)) => ""
      case Schema(parameter, storage, code) =>
        s"""parameter ${parameter.emit};
           |storage ${storage.emit};
           |code { ${emitInstructions(indent = 7)(code.instructions)} }""".stripMargin
    }
  }

  private def emitInstructions(indent: Int)(i: List[Instruction]): String =
    emitInstructions(" " * indent)(i)

  private def emitInstructions(indent: String)(i: List[Instruction]): String =
    i.map(_.emit)
      .mkString(" ;\n")
      .linesIterator
      .mkString("\n" + indent)
}

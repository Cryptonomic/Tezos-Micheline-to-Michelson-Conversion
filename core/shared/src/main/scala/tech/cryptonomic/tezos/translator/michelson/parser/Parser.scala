package tech.cryptonomic.tezos.translator.michelson.parser

import io.circe.Decoder
import io.circe.parser.decode
import tech.cryptonomic.tezos.translator.michelson.ast.{ Code, Expression, Instruction, Schema }
import tech.cryptonomic.tezos.translator.michelson.parser.Parser.Result

import scala.collection.immutable.{ List, Nil }

trait Parser[T] {
  def parse(raw: String): Result[T]
}

object Parser {
  type Result[T] = Either[Throwable, T]
  case class ParserError(message: String) extends Throwable(message)

  def parse[T: Parser](json: String): Result[T] =
    implicitly[Parser[T]].parse(sanitize(json))

  private def sanitize(s: String): String =
    s.filterNot(_.isControl)
      .replaceAll("""\\\\(u[a-zA-Z0-9]{1,4})""", "$1")
      .replaceAll("""\\(u[a-zA-Z0-9]{1,4})""", "$1")

  implicit val michelsonInstructionParser: Parser[Instruction] = {
    import GenericDerivation._
    decode[JsonInstruction](_).map(_.toMichelsonInstruction)
  }

  implicit val michelsonExpressionParser: Parser[Expression] = {
    import GenericDerivation._
    decode[JsonExpression](_).map(_.toMichelsonExpression)
  }

  implicit val michelsonSchemaParser: Parser[Schema] = {
    import GenericDerivation._

    val decoder = Decoder[List[JsonSection]].or(Decoder[LambdaForm].map(_.code))
    decode(_)(decoder).flatMap {
      case Nil          => Right(Schema.empty)
      case jsonSections => JsonSchema(jsonSections).toMichelsonSchema
    }
  }

  implicit val michelsonCodeParser: Parser[Code] = {
    import GenericDerivation._
    decode[List[JsonInstruction]](_).map(instructions => Code(instructions.map(_.toMichelsonInstruction)))
  }
}

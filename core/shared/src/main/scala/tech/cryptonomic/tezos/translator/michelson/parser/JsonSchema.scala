package tech.cryptonomic.tezos.translator.michelson.parser
import Parser.{ ParserError, Result }
import tech.cryptonomic.tezos.translator.michelson.ast._

case class JsonSchema(code: List[JsonSection]) {
  def toMichelsonSchema: Result[Schema] =
    for {
      parameter <- extractExpression("parameter")
      storage   <- extractExpression("storage")
      code      <- extractCode
    } yield Schema(parameter, storage, code)

  private def extractExpression(
    sectionName: String
  ): Result[Expression] =
    code.collectFirst {
      case it @ JsonExpressionSection(`sectionName`, _) => it
    }.flatMap(_.toMichelsonExpression)
      .toRight(ParserError(s"No expression $sectionName found"))

  private def extractCode: Result[Code] =
    code.collectFirst {
      case it @ JsonCodeSection("code", _) => it.toMichelsonCode
    }.toRight(ParserError("No code section found"))
}

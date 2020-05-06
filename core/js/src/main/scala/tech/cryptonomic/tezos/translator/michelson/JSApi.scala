package tech.cryptonomic.tezos.translator.michelson

import tech.cryptonomic.tezos.translator.michelson.ast.Schema

import scala.scalajs.js
import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }

@JSExportTopLevel("Translator")
object JSApi {
  @JSExport
  def translate(json: String): js.Object =
    MichelineToMichelson.convert[Schema](json) match {
      case Left(value) =>
        js.Dynamic.literal("status" -> "failure", "reason" -> value.getMessage)
      case Right(value) =>
        js.Dynamic.literal("status" -> "success", "result" -> value)
    }
}

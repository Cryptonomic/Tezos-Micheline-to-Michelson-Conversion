package tech.cryptonomic.tezos.translator.michelson.emitter

import scala.scalajs.js.JSON

trait EscapeJson {
  def escapeJsonString(s: String): String = JSON.stringify(s)
}

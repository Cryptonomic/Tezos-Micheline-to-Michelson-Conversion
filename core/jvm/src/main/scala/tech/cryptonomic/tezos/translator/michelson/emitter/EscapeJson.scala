package tech.cryptonomic.tezos.translator.michelson.emitter
import org.apache.commons.text.StringEscapeUtils

trait EscapeJson {
  def escapeJsonString(s: String): String = "\"%s\"".format(StringEscapeUtils.escapeJson(s))
}

package tech.cryptonomic.tezos.translator

import org.scalatest._
import org.scalatest.matchers.should.Matchers
import io.scalac.common.PlatformSpecificFiles
import tech.cryptonomic.tezos.translator.michelson.MichelineToMichelson
import tech.cryptonomic.tezos.translator.michelson.ast.Schema

class MichelineToMichelsonConseilJSSamplesSpec extends FlatSpec with Matchers with PlatformSpecificFiles {

  "JsonToMichelson" should "translate all samples from Micheline to Michelson" in {

    val samplesDir = "./core/shared/src/test/samples"

    val testFiles: Vector[String] = recursiveFiles(samplesDir).filter(_.endsWith(".micheline"))

    if (testFiles.isEmpty) fail("No test samples were loaded")

    testFiles.foreach { f: String =>
      println(s"Running test sample: $f")
      MichelineToMichelson.convert[Schema](loadFileContent(f)).isRight shouldBe true
    }
  }

}

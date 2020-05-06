package io.scalac.common

import scalajs.js.Dynamic.{ global => g, literal => o }
import scalajs.js

trait PlatformSpecificFiles {

  def recursiveFiles(dir: String): Vector[String] = {
    val fs = g.require("fs")

    def isDirectory(f: String) =
      fs.lstatSync(f).isDirectory().asInstanceOf[Boolean]

    val these = fs.readdirSync(dir).asInstanceOf[js.Array[String]].map(f => dir + "/" + f)
    (these.filterNot(isDirectory) ++
      these.filter(isDirectory).flatMap(recursiveFiles)).toVector
  }

  def loadFileContent(path: String): String = {
    val fs = g.require("fs")
    fs.readFileSync(path, o("encoding" -> "utf8")).asInstanceOf[String]
  }
}

package io.scalac.common

import java.io.File

trait PlatformSpecificFiles {

  def recursiveFiles(dir: String): Vector[String] = {
    val these = new File(dir).listFiles.toVector
    these.filterNot(_.isDirectory).map(_.getAbsolutePath) ++
      these.filter(_.isDirectory).flatMap(d => recursiveFiles(d.getAbsolutePath))
  }

  def loadFileContent(path: String): String = {
    val source = scala.io.Source.fromFile(path)
    try source.mkString
    finally source.close()
  }
}

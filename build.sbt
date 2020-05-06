import sbtcrossproject.CrossPlugin.autoImport.crossProject
import scalajsbundler.util.JSON._
import scala.sys.process._

lazy val commonSettings = Seq(
  organization := "io.scalac",
  scalaVersion := "2.13.1",
  crossScalaVersions := Seq("2.12.11", "2.13.1")
)

lazy val circeVersion = "0.13.0"

lazy val catsVersion = "2.2.0-M1"

lazy val scalaTestVersion = "3.1.1"

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .in(file("core"))
  .settings(commonSettings: _*)
  .settings(
    name := "core",
    libraryDependencies ++= Seq(
      "io.circe"      %%% "circe-core"    % circeVersion,
      "io.circe"      %%% "circe-parser"  % circeVersion,
      "io.circe"      %%% "circe-generic" % circeVersion,
      "org.typelevel" %%% "cats-core"     % catsVersion,
      "org.scalatest" %%% "scalatest"     % scalaTestVersion % "test"
    )
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-text" % "1.7"
    )
  )
  .jsConfigure(_.enablePlugins(ScalaJSBundlerPlugin))
  .jsSettings(
    webpackBundlingMode := BundlingMode.LibraryAndApplication(),
    npmExtraArgs in Compile := Seq("-silent"),
    additionalNpmConfig in Compile := Map(
      "name"    -> str("tezos-micheline-to-michelson"),
      "version" -> str("0.1.0")
    ),
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.0.0-RC5" % Test
    ),
    buildJS.in(Compile) := {
      (webpack in (Compile, fullOptJS)).value
      val buildDir = target.value / "build"
      s"rm -rf $buildDir" !;
      s"mkdir $buildDir -p" !;
      s"cp ${crossTarget.value}/scalajs-bundler/main/package.json             $buildDir/package.json" !;
      s"cp ${crossTarget.value}/scalajs-bundler/main/${name.value}-opt.js     $buildDir/index.js" !;
      s"cp ${crossTarget.value}/scalajs-bundler/main/${name.value}-opt.js.map $buildDir/index.js.map" !
    }
  )

lazy val coreJVM = core.jvm
lazy val coreJS  = core.js
lazy val buildJS = taskKey[Unit]("Prepare a production js build")

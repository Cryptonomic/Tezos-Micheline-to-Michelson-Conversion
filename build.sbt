import sbtcrossproject.CrossPlugin.autoImport.crossProject
import scalajsbundler.util.JSON._
import ReleaseTransformations._
import ReleasePlugin.autoImport._

import scala.language.postfixOps
import scala.sys.process._

val compilerOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-unchecked",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xfuture",
  "-Xfatal-warnings",
  "-Ywarn-unused-import"
)

def priorTo2_13(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, minor)) if minor < 13 => true
    case _                              => false
  }

lazy val commonSettings = Seq(
  organization := "ru.pavkin",
  scalaVersion := "2.13.3",
  crossScalaVersions := Seq("2.12.12", "2.13.3"),
  scalacOptions ++= {
    if (priorTo2_13(scalaVersion.value)) compilerOptions
    else
      compilerOptions.flatMap {
        case "-Ywarn-unused-import" => Seq("-Ywarn-unused:imports")
        case "-Xfuture"             => Nil
        case other                  => Seq(other)
      }
  }
)

lazy val circeVersion = "0.14.0-M1"

lazy val catsVersion = "2.2.0-RC3"

lazy val scalaTestVersion = "3.1.1"

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .in(file("core"))
  .settings(commonSettings: _*)
  .settings(
    name := "core",
    moduleName := "tezos-micheline-to-michelson",
    libraryDependencies ++= Seq(
      "io.circe"      %%% "circe-core"    % circeVersion,
      "io.circe"      %%% "circe-parser"  % circeVersion,
      "io.circe"      %%% "circe-generic" % circeVersion,
      "org.typelevel" %%% "cats-core"     % catsVersion,
      "org.scalatest" %%% "scalatest"     % scalaTestVersion % "test"
    )
  )
  .settings(publishSettings)
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-text" % "1.7"
    )
  )
  .jsConfigure(_.enablePlugins(ScalaJSBundlerPlugin))
  .jsSettings(
    webpackBundlingMode := BundlingMode.LibraryAndApplication(),
    npmExtraArgs in Compile := Seq("-silent"),
    additionalNpmConfig in Compile := {
      Map(
        "name"    -> str(moduleName.value),
        "version" -> str(version.value)
      )
    },
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.0.0" % Test
    ),
    buildJS.in(Compile) := {
      (webpack in (Compile, fullOptJS)).value
      val buildDir = target.value / "build"
      s"rm -rf $buildDir" !;
      s"mkdir $buildDir -p" !;
      s"cp ${crossTarget.value}/scalajs-bundler/main/package.json                   $buildDir/package.json" !;
      s"cp ${crossTarget.value}/scalajs-bundler/main/${moduleName.value}-opt.js     $buildDir/index.js" !;
      s"cp ${crossTarget.value}/scalajs-bundler/main/${moduleName.value}-opt.js.map $buildDir/index.js.map" !
    },
    publishNPM.in(Compile) := {
      (buildJS in Compile).value
      s"npm publish ${target.value / "build"}" !
    }
  )

lazy val coreJVM    = core.jvm
lazy val coreJS     = core.js
lazy val buildJS    = taskKey[Unit]("Prepare a production js build")
lazy val publishNPM = taskKey[Unit]("Publish NPM package to the registry")

lazy val root = project
  .in(file("."))
  .settings(commonSettings: _*)
  .settings(publishSettings)
  .settings(noPublishSettings)
  .aggregate(coreJVM, coreJS)
  .dependsOn(coreJVM, coreJS)

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  publishMavenStyle := true,
  publishTo := sonatypePublishToBundle.value
)

lazy val publishSettings = Seq(
  releaseUseGlobalVersion := true,
  releaseVersionFile := file(".") / "version.sbt",
  releaseCommitMessage := s"Set version to ${version.value}",
  releaseIgnoreUntrackedFiles := true,
  releaseCrossBuild := true,
  homepage := Some(url("https://github.com/Cryptonomic/Tezos-Micheline-to-Michelson-Conversion")),
  licenses := Seq("GPL 3.0" -> url("https://www.gnu.org/licenses/gpl-3.0.en.html")),
  publishTo := sonatypePublishToBundle.value,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/Cryptonomic/Tezos-Micheline-to-Michelson-Conversion"),
      "scm:git:git@github.com:Cryptonomic/Tezos-Micheline-to-Michelson-Conversion.git"
    )
  ),
  developers := List(
    Developer(
      id = "vpavkin",
      name = "Vladimir Pavkin",
      email = "vladimir.pavkin@scalac.io",
      url = url("http://pavkin.ru")
    )
  ),
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommand("buildJS"),
    releaseStepCommand("publishNPM"),
    releaseStepCommandAndRemaining("+publishSigned"),
    releaseStepCommand("sonatypeBundleRelease"),
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
)

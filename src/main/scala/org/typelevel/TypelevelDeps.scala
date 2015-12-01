package org
package typelevel

import sbt._
import sbtcatalysts.CatalystsPlugin.autoImport._

/** Default Typelevel dependencies.*/
object Dependencies {

  /**
   * Current versions for libraries and packages
   *
   * Format: Package -> version
   */
  val versions = Map[String, String](
    "algebra"        -> "0.3.1",
    "alleycats"      -> "0.1.2",
    "catalysts"      -> "0.1.0",
    "cats"           -> "0.3.0",
    "discipline"     -> "0.4",
    "export-hook"    -> "1.1.0",
    "kind-projector" -> "0.7.1",
    "machinist"      -> "0.4.1",
    "macro-compat"   -> "1.1.0",
    "monocle"        -> "1.2.0-M2",
    "paradise"       -> "2.1.0-M5",
    "refined"        -> "0.3.2",
    "sbt-catalysts"  -> "0.1.6",
    "scalacheck"     -> "1.12.5",
    "scalatest"      -> "3.0.0-M11",
    "scalac"         -> "2.11.7",
    "scalac_2.11"    -> "2.11.7",
    "scalac_2.10"    -> "2.10.6",
    "shapeless"      -> "2.2.5",
    "simulacrum"     -> "0.5.0",
    "specs2"         -> "3.6.5"
  )

  /**
   * TEMPORARY: Repos for current libraries and packages
   *
   * As is, there is obvious duplication with the version maps. However, this section
   * will change dramatically once dbuild is implemented as we need to store per verion build
   * details. Could be that we need one file per repo with all the details, or at least
   * one map per package. Who knows. Right now, this all for a "frozen" state, should be
   * per package version.
   * 
   * Format: Package -> (repo, git branch/hash, version)
   */
  val repos = Map[String, (String, String, String)](
    "algebra"        -> ("github.com/non/algebra"              , "v0.3.1"           , "0.3.1"),
    "alleycats"      -> ("github.com/non/alleycats"            , "v0.1.2"           , "0.1.2"),
    "catalysts"      -> ("github.com/InTheNow/catalysts"       , "v0.1.0"           , "0.1.0"),
    "cats"           -> ("github.com/non/cats"                 , "v0.3.0"           , "0.3.0"),
    "discipline"     -> ("github.com/typelevel/discipline"     , "v0.4"             , "0.4"),
    "export-hook"    -> ("github.com/milessabin/export-hook"   , "v1.1.0"           , "1.1.0"),
    "kind-projector" -> ("github.com/non/kind-projector"       , "v0.7.1"           , "0.7.1"),
    "machinist"      -> ("github.com/typelevel/machinist"      , "v0.4.1"           , "0.4.1"),
    "macro-compat"   -> ("github.com/milessabin/macro-compat"  , "v1.1.0"           , "1.1.0"),
    "monocle"        -> ("github.com/julien-truffaut/Monocle"  , "v1.2.0-M2"        , "1.2.0-M2"),
    "paradise"       -> ("github.com/scalamacros/paradise"     , "v2.1.0_2.11.7"    , "2.1.0-M5"),
    "refined"        -> ("github.com/fthomas/refined"          , "v0.3.2"           , "0.3.2"),
    "sbt-catalysts"  -> ("github.com/InTheNow/sbt-catalysts"   , "0.1.6"            , "0.1.6"),
    "scalacheck"     -> ("github.com/rickynils/scalacheck"     , "1.12.5"           , "1.12.5"),
    "scalatest"      -> ("github.com/scalatest/scalatest"      , "3.0.x"            , "3.0.0-M10"),
    "scalac"         -> ("github.com/scala/scala"              , "v2.11.7"          , "2.11.7"),
    "scalac_2.11"    -> ("github.com/scala/scala"              , "v2.11.7"          , "2.11.7"),
    "scalac_2.10"    -> ("github.com/scala/scala"              , "v2.10.6"          , "2.10.6"),
    "shapeless"      -> ("github.com/milessabin/shapeless"     , "shapeless-2.2.5"  , "2.2.5"),
    "simulacrum"     -> ("github.com/mpilquist/simulacrum"     , "v0.5.0"           , "0.5.0"),
    "specs2"         -> ("github.com/etorreborre/specs2"       , "SPECS2-3.6.5"     , "3.6.5")
  )

  /**
   * Library definitions and links to their versions.
   *
   * Note that one version may apply to more than one library.
   * Format: Library name -> version key, org, library
   */
  val libraries = Map[String, (String, String, String)](
    "algebra"               -> ("algebra"         , "org.spire-math"               , "algebra"),
    "algebra-laws"          -> ("algebra"         , "org.spire-math"               , "algebra-laws"),
    "algebra-std"           -> ("algebra"         , "org.spire-math"               , "algebra-std"),
    "alleycats"             -> ("alleycats"       , "org.typelevel"                , "alleycats"),
    "catalysts"             -> ("catalysts"       , "org.typelevel"                , "catalysts"),
    "catalysts-checklite"   -> ("catalysts"       , "org.typelevel"                , "catalysts-checklite"),
    "catalysts-lawkit"      -> ("catalysts"       , "org.typelevel"                , "catalysts-lawkit"),
    "catalysts-macros"      -> ("catalysts"       , "org.typelevel"                , "catalysts-macros"),
    "catalysts-platform"    -> ("catalysts"       , "org.typelevel"                , "catalysts-platform"),
    "catalysts-scalatest"   -> ("catalysts"       , "org.typelevel"                , "catalysts-scalatest"),
    "catalysts-specbase"    -> ("catalysts"       , "org.typelevel"                , "catalysts-specbase"),
    "catalysts-speclite"    -> ("catalysts"       , "org.typelevel"                , "catalysts-speclite"),
    "catalysts-specs2"      -> ("catalysts"       , "org.typelevel"                , "catalysts-specs2"),
    "catalysts-testkit"     -> ("catalysts"       , "org.typelevel"                , "catalysts-testkit"),
    "cats"                  -> ("cats"            , "org.spire-math"               , "cats"),
    "cats-core"             -> ("cats"            , "org.spire-math"               , "cats-core"),
    "cats-free"             -> ("cats"            , "org.spire-math"               , "cats-free"),
    "cats-laws"             -> ("cats"            , "org.spire-math"               , "cats-laws"),
    "cats-macros"           -> ("cats"            , "org.spire-math"               , "cats-macros"),
    "cats-state"            -> ("cats"            , "org.spire-math"               , "cats-state"),
    "discipline"            -> ("discipline"      , "org.typelevel"                , "discipline"),
    "export-hook"           -> ("export-hook"     , "org.typelevel"                , "export-hook"),
    "machinist"             -> ("machinist"       , "org.typelevel"                , "machinist"),
    "macro-compat"          -> ("macro-compat"    , "org.typelevel"                , "macro-compat"),
    "monocle-core"          -> ("monocle"         , "com.github.julien-truffaut"   , "monocle-core"),
    "monocle-generic"       -> ("monocle"         , "com.github.julien-truffaut"   , "monocle-generic"),
    "monocle-macro"         -> ("monocle"         , "com.github.julien-truffaut"   , "monocle-macro"),
    "monocle-state"         -> ("monocle"         , "com.github.julien-truffaut"   , "monocle-state"),
    "monocle-law"           -> ("monocle"         , "com.github.julien-truffaut"   , "monocle-law"),
    "refined"               -> ("refined"         , "eu.timepit"                   , "refined"),
    "refined-scalacheck"    -> ("refined"         , "eu.timepit"                   , "refined-scalacheck"),
    "refined-scalaz"        -> ("refined"         , "eu.timepit"                   , "refined-scalaz"),
    "refined-scodec"        -> ("refined"         , "eu.timepit"                   , "refined-scodec"),
    "scalatest"             -> ("scalatest"       , "org.scalatest"                , "scalatest"),
    "scalacheck"            -> ("scalacheck"      , "org.scalacheck"               , "scalacheck"),
    "shapeless"             -> ("shapeless"       , "com.chuusai"                  , "shapeless"),
    "simulacrum"            -> ("simulacrum"      , "com.github.mpilquist"         , "simulacrum"),
    "specs2-core"           -> ("specs2"          , "org.specs2"                   , "specs2-core"),
    "specs2-scalacheck"     -> ("specs2"          , "org.specs2"                   , "specs2-scalacheck")
  )

  /**
   * Compiler plugins definitions and links to their versions
   *
   * Note that one version may apply to more than one plugin.
   *
   * Format: Library name -> version key, org, librar, crossVersion
   */
  val scalacPlugins = Map[String, (String, String, String, CrossVersion)](
    "kind-projector"    -> ("kind-projector"  , "org.spire-math"      , "kind-projector" , CrossVersion.binary),
    "paradise"          -> ("paradise"        , "org.scalamacros"     , "paradise"       , CrossVersion.full)
  )

  // Some helper methods to combine libraries
  /**
   * Sets all settings required for the macro-compat library.
   *
   * @param v Versions map to use
   * @return All settings required for the macro-compat library
   */
  def macroCompatSettings(v: Dependencies): Seq[Setting[_]] =
    addCompileLibs(v, "macro-compat") ++ addCompilerPlugins(v, "paradise") ++
      scalaMacroDependencies(v)
}

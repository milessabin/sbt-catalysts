package sbtcatalysts

import sbt.Keys._
import sbt._

/**
 * Plugin that automatically brings into scope all predefined val's and method
 * definitions.
 * 
 * It is very important to realise that using the plugin does not actually change the
 * the build in any way, but merely provides the methods to help create a build.sbt.
 * 
 * Compared to a plugin that "does everything", the advantage of this approach is that it is far
 * easier to use other functionality where appropriate and also to "see" what the build is
 * is actually doing, as the methods are (mainly) just pure SBT methods.
 * 
 * Where we do deviate from "pure" SBT is how we define library dependencies. The norm would be:
 * 
 * In a small project, define the library dependencies explicitly:
 * 
 *    libraryDependencies += "org.typelevel" %% "alleycats" %  "0.1.0"
 * 
 * If another sub-project also has this dependency, it's common to move the definition to a val, and
 * often the version too. If the library is used in another module for test only, another val is 
 * required:
 * 
 *      val alleycatsV = "0.1.0"
 *      val alleycatsDeps = Seq(libraryDependencies += "org.typelevel" %% "alleycats" % alleycatsV)
 *      val alleycatsTestDeps = Seq(libraryDependencies += "org.typelevel" %% "alleycats" % alleycatsV % "test")
 * 
 * Whilst this works fine for individual projects, it's not ideal when a group of loosely coupled want
 * to share a common set (or sets) of dependencies, that they can also modify locally if required.
 * In this sense, we need "cascading configuration dependency files" and this is what this plugin also
 * provides.
 * 
 * "org.typelevel.depenendcies" provides two Maps, one for library versions the the for individual libraries
 * with their organisation, name and version. Being standard scala Maps, other dependency Maps can be added
 * with new or updated dependencies. The same applies for scala plugins. To use, we create the three Maps and
 * add to a combined container and then add the required dependencies to a module: eg
 * 
 *     val vers = typelevel.versions ++ catalysts.versions + ("discipline" -> "0.3")
 *     val libs = typelevel.libraries ++ catalysts.libraries
 *     val addins = typelevel.scalacPlugins ++ catalysts.scalacPlugins
 *     val vAll = Versions(vers, libs, addins)
 *     ....
 *    .settings(addLibs(vAll, "specs2-core","specs2-scalacheck" ):_*)
 *    .settings(addTestLibs(vAll, "scalatest" ):_*)
 * 
 */
object CatalystsPlugin extends AutoPlugin {
 
  object autoImport extends CatalystsSbt
  import autoImport._

  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  override def globalSettings: Seq[Def.Setting[_]] = Seq(
    commands ++= Seq(catalystsPrintMarkdown, catalystsShowLibraries, catalystsShowPackages, catalystsShowProjectVersions)
  )

  override def buildSettings: Seq[Setting[_]] =
    addCommandAlias("gitSnapshots", ";set version in ThisBuild := git.gitDescribedVersion.value.get + \"-SNAPSHOT\"")
}

/** Exposes the settings and commands directly available in the SBT Console.*/
trait CatalystsSbt extends Catalysts {

  lazy val catalystsSettings = settingKey[CatalystsSettings]("The ...")

  lazy val catalystsProjectVersions = settingKey[VersionsType]("The dependencies")

  def catalystsShowLibraries = Command.command("catalystsShowLibraries") { state =>

    val extracted = Project.extract(state)
    import extracted._

    val settings = (catalystsSettings in currentRef get structure.data).get
    prettyPrintLibs(settings)
    state
  }

  def catalystsShowPackages = Command.command("catalystsShowPackages") { state =>

    val extracted = Project.extract(state)
    import extracted._

    val info = (catalystsSettings in currentRef get structure.data).get
    val versions = info.versions
    prettyPrintPackages(versions.vers, versions.repos)
    state
  }
  def catalystsShowProjectVersions = Command.command("catalystsShowProjectVersions") { state =>

    val extracted = Project.extract(state)
    import extracted._
    val prjVersions = (catalystsProjectVersions in currentRef get structure.data).get

    val info = (catalystsSettings in currentRef get structure.data).get
    val versions = info.versions

    prettyPrintPackages(prjVersions, versions.repos)

    state
  }

  def catalystsPrintMarkdown = Command.command("catalystsPrintMarkdown") { state =>

    val extracted = Project.extract(state)
    import extracted._
    val prjVersions = (catalystsProjectVersions in currentRef get structure.data).get

    val info = (catalystsSettings in currentRef get structure.data).get
    val versions = info.versions

    val repoInfo = versions.repos("sbt-catalysts")
    val repo = repoInfo._1
    val proj = info.gh.properName
    val sbtCatalystsV = repoInfo._2

    val header = s"""
      |$proj uses [sbt-catalysts][] $sbtCatalystsV to provide a consistent view of
      |its dependencies on Typelevel projects. As of sbt-catalysts $sbtCatalystsV the
      |base version profile can be found [here][typelevel-deps]. $proj
      |uses the following version overrides relative to that,
      |""".stripMargin

    println(header)
    prettyPrintPackages(prjVersions, versions.repos)
    println()
    println(s"[sbt-catalysts]: https://$repo")
    println(s"[typelevel-deps]: https://$repo/blob/$sbtCatalystsV/src/main/scala/org/typelevel/TypelevelDeps.scala")
    println()

    state
  }

  private def libs(pkg: String, settings: CatalystsSettings): String = {
     val libs = settings.versions.libs.filter(l => l._2._1 == pkg)
     libs.values.map(m => m._3).mkString(",")
  }

  private def prettyPrintLibs(settings: CatalystsSettings): Unit = {
    val versions = settings.versions.vers
    val repo = settings.versions.repos
    versions.toList.sorted.foreach(s => println(s"+ [${s._1}-${s._2}](https://${repo(s._1)._1}) ${libs(s._1, settings)}"))
  }

  private def prettyPrintPackages(versions: VersionsType, repo: RepositoriesType): Unit = {
    versions.toList.sorted.foreach(s => println(s"+ [${s._1}-${s._2}](https://${repo(s._1)._1})"))
  }
}

package sbtcatalysts

import sbt._
import Keys._

import com.typesafe.sbt.pgp.PgpKeys
import com.typesafe.sbt.SbtSite.SiteKeys._
import com.typesafe.sbt.SbtSite.site
import com.typesafe.sbt.SbtGit._
import com.typesafe.sbt.SbtGhPages.GhPagesKeys._
import com.typesafe.sbt.SbtGhPages.ghpages
import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import com.typesafe.tools.mima.plugin.MimaKeys
import com.typesafe.tools.mima.plugin.MimaKeys.{previousArtifacts, binaryIssueFilters}
import com.typesafe.tools.mima.core._
import com.typesafe.tools.mima.core.ProblemFilters._

import org.scalajs.sbtplugin.cross.{CrossProject, CrossType}
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtunidoc.Plugin._
import sbtunidoc.Plugin.UnidocKeys._
import scoverage.ScoverageSbtPlugin._
import tut.Plugin._

/** Contains all methods to simplify a common build file.*/
trait Catalysts {
  type VersionsType = Map[String, String]
  type LibrariesType = Map[String, (String, String, String)]
  type RepositoriesType = Map[String, (String, String, String)]
  type ScalacPluginType = Map[String, (String, String, String, CrossVersion)]

  case class CatalystsSettings(gh: GitHubSettings, devs: Seq[Dev], versions: Dependencies)

  /** Container for the version, library and scala plugin Maps.*/
  case class Dependencies(vers: VersionsType, libs: LibrariesType, repos:RepositoriesType, plugs: ScalacPluginType) {
    def vLibs  = (vers, libs)
    def vPlugs = (vers, plugs)
    def vRepos = (vers, repos)
  }

  /** Github settings and related settings usually found in a Github README.*/
  case class GitHubSettings(org: String, proj: String, properName: String, publishOrg: String, license: (String, URL)) {
    def home = s"https://github.com/$org/$proj"
    def repo = s"git@github.com:$org/$proj.git"
    def api  = s"https://$org.github.io/$proj/api/"
    def organisation  = s"com.github.$org"
    override def toString =
      s"GitHubSettings:home = $home\nGitHubSettings:repo = $repo\nGitHubSettings:api = $api\nGitHubSettings:organisation = $organisation"
  }

  /** The name and github user id */
  //From https://github.com/typelevel/sbt-typelevel/blob/master/src/main/scala/Developer.scala
  case class Dev(name: String, id: String) {
    def pomExtra: xml.NodeSeq =
      <developer>
        <id>{ id }</id>
        <name>{ name }</name>
        <url>http://github.com/{ id }</url>
      </developer>
  }

  /** Using the supplied Versions map, adds the list of libraries to a module.*/
  def addLibs(versions: Dependencies, libs: String*) =
    libs.flatMap(s => Seq(libraryDependencies +=
      versions.libs(s)._2 %%% versions.libs(s)._3 % versions.vers(versions.libs(s)._1)))

  /** Using the supplied Versions map, adds the list of libraries to a module as a compile dependency.*/
  def addCompileLibs(versions: Dependencies, libs: String*) = addLibsScoped(versions, "compile", libs:_*)

  /** Using the supplied Versions map, adds the list of libraries to a module as a test dependency.*/
  def addTestLibs(versions: Dependencies, libs: String*) = addLibsScoped(versions, "test", libs:_*)

  /** Using versions map, adds the list of libraries to a module using the given dependency.*/
  def addLibsScoped(versions: Dependencies, scope: String, libs: String*) =
    libs.flatMap(s => Seq(libraryDependencies +=
      versions.libs(s)._2 %%% versions.libs(s)._3 % versions.vers(versions.libs(s)._1) % scope))

  /** Using the supplied Versions map, adds the list of compiler plugins to a module.*/
  def addCompilerPlugins(v: Dependencies, plugins: String*) =
    plugins.flatMap(s => Seq(libraryDependencies += compilerPlugin(
      v.plugs(s)._2 %% v.plugs(s)._3 % v.vers(v.plugs(s)._1) cross v.plugs(s)._4)))

  // Licences
  /** Apache 2.0 Licence.*/
  val apache = ("Apache License", url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

  /** MIT Licence.*/
  val mit = ("MIT", url("http://opensource.org/licenses/MIT"))

  // Common and shared setting
  /** Settings to make the module not published*/
  lazy val noPublishSettings = Seq(
    publish := (),
    publishLocal := (),
    publishArtifact := false
  )

  /**
   * Default settings for the root module.
   *
   * Sets the root module name to root and sets the module not to be published
   */
  lazy val rootSettings = noPublishSettings ++ Seq(
    moduleName := "root"
  )

  /** Adds the settings required to add scala versioned shared directory.
    *
    * In a scala.js shared project, the shared sources are by default in a
    * the directory "shared/src/main/scala". By adding these settings, the build will also
    * look in the directories that match the scala version:
    *
    *      "shared/src/main/scala_2.10"
    *      "shared/src/main/scala_2.11"
    */
  lazy val crossVersionSharedSources: Seq[Setting[_]] =
    Seq(Compile, Test).map { sc =>
      (unmanagedSourceDirectories in sc) ++= {
        (unmanagedSourceDirectories in sc ).value.map {
          dir:File => new File(dir.getPath + "_" + scalaBinaryVersion.value)
        }
      }
    }

  /** Using the supplied Versions map, adds the dependencies for scala macros.*/
  def scalaMacroDependencies(v: Dependencies): Seq[Setting[_]] = {
    val version = v.vers("paradise")
    Seq(
      libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
      libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
      libraryDependencies ++= {
        CrossVersion.partialVersion(scalaVersion.value) match {
          // if scala 2.11+ is used, quasiquotes are merged into scala-reflect
          case Some((2, scalaMajor)) if scalaMajor >= 11 => Seq()
          // in Scala 2.10, quasiquotes are provided by macro paradise
          case Some((2, 10)) =>
            Seq(
              compilerPlugin("org.scalamacros" %% "paradise" % version cross CrossVersion.full),
              "org.scalamacros" %% "quasiquotes" % version cross CrossVersion.binary
            )
        }
      }
    )
  }

  /** Common scalac options useful to most (if not all) projects.*/
  lazy val scalacCommonOptions = Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-unchecked",
    "-Xlint"
  )

  /** Scalac options for additional language options.*/
  lazy val scalacLanguageOptions = Seq(
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:experimental.macros"
  )

  /** Scalac strict compilation options.*/
  lazy val scalacStrictOptions = Seq(
    "-Xfatal-warnings",
    "-Yinline-warnings",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture"
  )

  /** Combines all scalac options.*/
  lazy val scalacAllOptions = scalacCommonOptions ++ scalacLanguageOptions ++ scalacStrictOptions

  /**
   * Settings common to all projects.
   *
   * Adds Sonatype release repository and "withCachedResolution" to the update options
   */
  lazy val sharedCommonSettings: Seq[sbt.Setting[_]] = Seq(
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases")
    ),
    updateOptions := updateOptions.value.withCachedResolution(true)
  )

  /**
   * Build settings common to all projects.
   *
   * Uses the github settings and versions map to set the organisation,
   * scala version and cross versions
   */
  def sharedBuildSettings(gh: GitHubSettings, v: Dependencies) = Seq(
    organization := gh.publishOrg,
    scalaVersion := v.vers("scalac"),
    crossScalaVersions := Seq(v.vers("scalac_2.10"), scalaVersion.value)
  )

  /**
   * Publish settings common to all projects.
   *
   * Uses the github settings and list of developers to set all publish settings
   * required to publish signed artifacts to Sonatype OSS repository
   */
  def sharedPublishSettings(gh: GitHubSettings, devs: Seq[Dev]): Seq[Setting[_]] = Seq(
    homepage := Some(url(gh.home)),
    licenses += gh.license,
    scmInfo :=  Some(ScmInfo(url(gh.home), "scm:git:" + gh.repo)),
    apiURL := Some(url(gh.api)),
    releaseCrossBuild := true,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := Function.const(false),
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("Snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("Releases" at nexus + "service/local/staging/deploy/maven2")
    },
    autoAPIMappings := true,
    pomExtra := <developers> { devs.map(_.pomExtra) } </developers>
  )

  /**
   * Shared mima settings
   *
   *   import com.typesafe.tools.mima.core.ProblemFilters
   *   import com.typesafe.tools.mima.core._
   */
  def sharedMimaSettings(version: String = "", filters: Seq[ProblemFilter] = Seq()): Seq[Setting[_]] = {
    val prevArtifacts =
      if (version.isEmpty())
        Seq(previousArtifacts := Set())
      else
        Seq(previousArtifacts := Set(organization.value %% moduleName.value % version))

    mimaDefaultSettings ++ prevArtifacts ++ Seq(binaryIssueFilters ++= filters)
  }

  /**
   * Release settings common to all projects.
   *
   * Adds a Sonatype release step to the default release steps
   */
  lazy val sharedReleaseProcess = Seq(
    releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
    pushChanges)
  )

  /** Common coverage settings, with minimum coverage defaulting to 80.*/
  def sharedScoverageSettings(min: Int = 80) = Seq(
    ScoverageKeys.coverageMinimum := min,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := scalaBinaryVersion.value != "2.10"
    // ScoverageKeys.coverageExcludedPackages := "catalysts\\.bench\\..*"
  )

  /** Common unidoc settings, adding the "-Ymacro-no-expand" scalac option.*/
  lazy val unidocCommonSettings = Seq(
    scalacOptions in (ScalaUnidoc, unidoc) += "-Ymacro-no-expand"
  )

  /** Add the "unused import" warning to scala 2.11+, but not for the console.*/
  lazy val warnUnusedImport = Seq(
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 10)) =>
          Seq()
        case Some((2, n)) if n >= 11 =>
          Seq("-Ywarn-unused-import")
      }
    },
    //use this when activator moved to 13.9
    // scalacOptions in (Compile, console) -= "-Ywarn-unused-import",
    scalacOptions in (Compile, console) ~= {_.filterNot("-Ywarn-unused-import" == _)},
    scalacOptions in (Test, console) <<= (scalacOptions in (Compile, console))
  )

  /** Adds the credential settings required for sonatype releases.*/
  lazy val credentialSettings = Seq(
    // For Travis CI - see http://www.cakesolutions.net/teamblogs/publishing-artefacts-to-oss-sonatype-nexus-using-sbt-and-travis-ci
    credentials ++= (for {
      username <- Option(System.getenv().get("SONATYPE_USERNAME"))
      password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
    } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq
  )

  // Builder methods

  /**
   * Creates a module definition based on a Scala.js CrossProject
   *
   * The standard Scala.js CrossProject contains the real JVM and JS project
   * and can be used as a dependency of another CrossProject. But it not an
   * sbt Project, and cannot be used as one.
   *
   * So we introduce the concept of a Module, that is essentially a CrossProject
   * with the difference that the variable name is the directory name with the
   * suffix "M". This allows us to create a real project based on the directory name
   * that is an aggregate project for the underlying JS and JVM projects.
   *
   * Usage;
   *
   * In build.sbt, create two helper methods:
   *
   *   lazy val module = mkModuleFactory(gh.proj, mkConfig(rootSettings, commonJvmSettings, commonJsSettings))
   *   lazy val prj = mkPrjFactory(rootSettings)
   *
   * For each sub-project (here core is used as an example)
   *
   *   lazy val core    = prj(coreM)
   *   lazy val coreJVM = coreM.jvm
   *   lazy val coreJS  = coreM.js
   *   lazy val coreM   = module("core",CrossType.Pure)
   *     .dependsOn(testkitM)
   *     .settings(addTestLibs(vlibs, "scalatest"):_*)
   */
  def mkModule(proj: String, projConfig: CrossProject ⇒ CrossProject, id: String,
               crossType: CrossType = CrossType.Pure ): CrossProject = {

    val cpId = id.stripSuffix("M")

    CrossProject(cpId, new File(cpId), crossType)
      .settings(moduleName := s"$proj-$id")
      .configure(projConfig)
  }

  /**
   * Helper method for mkModule so that the main project name and configuration need not ne repeated for each module
   */
  def mkModuleFactory(proj: String, projConfig: CrossProject ⇒ CrossProject) =
    (id: String, crossType: CrossType) => mkModule(proj, projConfig, id, crossType)

  /**
   * Makes a modules aggregate project
   */
  def mkPrj(projSettings: Seq[sbt.Setting[_]], c: CrossProject): Project = {
    val name = c.jvm.id.stripSuffix("JVM")
    val pr: Seq[ProjectReference] = Seq(c.jvm.project,  c.js.project)

    Project(id = name, base = new File(s"${name}/.prj"))
      .settings(noPublishSettings:_*)
      .settings(projSettings)
      .dependsOn(c.jvm, c.js)
      .aggregate(c.jvm, c.js)
  }

  /**
   * Helper method for mkPrj so that the main project settings need not be repeated for each module
   */
  def mkPrjFactory(projSettings: Seq[sbt.Setting[_]]) = (c: CrossProject) => mkPrj(projSettings, c)

  // Config builders

  /**
   * Helper method that sets the default project(i.e. platform independent), JS and JVM settings.
   */
  def mkConfig(projSettings: Seq[sbt.Setting[_]], jvmSettings: Seq[sbt.Setting[_]],
               jsSettings: Seq[sbt.Setting[_]] ): CrossProject ⇒ CrossProject =
    _.settings(projSettings:_*)
      .jsSettings(jsSettings:_*)
      .jvmSettings(jvmSettings:_*)

  /**
   * Helper method that sets the root project's settings
   *
   * In addition to setting the root settings, also adds the rootJVM settings to be used in
   * in root console.
   */
  def mkRootConfig(projSettings: Seq[sbt.Setting[_]] , projJVM:Project): Project ⇒ Project =
    _.in(file("."))
      .settings(rootSettings)
      .settings(projSettings)
      .settings(console <<= console in (projJVM, Compile))

  /**
   * Creates the rootJVM project.
   *
   * Creates the rootJVM project in ".rootJVM" with the default settings and
   * JVM specific default settings.
   */
  def mkRootJvmConfig(s: String, projSettings: Seq[sbt.Setting[_]],
                      jvmSettings: Seq[sbt.Setting[_]]): Project ⇒ Project =
    _.settings(moduleName := s)
      .settings(projSettings)
      .settings(jvmSettings)
      .in(file("." + s + "JVM"))


  /**
   * Creates the rootJS project.
   *
   * Creates the rootJS project in ".rootJS" with the default settings and
   * JS specific default settings.
   */
  def mkRootJsConfig(s: String, projSettings: Seq[sbt.Setting[_]],
                     jsSettings: Seq[sbt.Setting[_]]): Project ⇒ Project =
    _.settings(moduleName := s)
      .settings(projSettings)
      .settings(jsSettings)
      .in(file("." + s + "JS"))
      .enablePlugins(ScalaJSPlugin)

  /**
   * Creates a configuration for a document project for scaladoc and a GitHubPages site
   *
   * Creates the basic settings for a document project based on the supplied GitHub settings,
   * project and JVM settings. The documentation is created for the supplied list of projects.
   */
  def mkDocConfig(gh: GitHubSettings, projSettings: Seq[sbt.Setting[_]], jvmSettings: Seq[sbt.Setting[_]],
                  deps: Project*): Project ⇒ Project =
    _.settings(projSettings)
      .settings(moduleName := gh.proj + "-docs")
      .settings(noPublishSettings)
      .settings(unidocSettings)
      .settings(site.settings)
      .settings(tutSettings)
      .settings(ghpages.settings)
      .settings(jvmSettings)
      .dependsOn(deps.map( ClasspathDependency(_, Some("compile;test->test"))):_*)
      .settings(
        organization  := gh.organisation,
        autoAPIMappings := true,
        unidocProjectFilter in (ScalaUnidoc, unidoc) := inProjects(deps.map(_.project)  :_*),
        site.addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), "api"),
        ghpagesNoJekyll := false,
        site.addMappingsToSiteDir(tut, "_tut"),
        tutScalacOptions ~= (_.filterNot(Set("-Ywarn-unused-import", "-Ywarn-dead-code"))),

        scalacOptions in (ScalaUnidoc, unidoc) ++= Seq(
          "-doc-source-url", scmInfo.value.get.browseUrl + "/tree/master€{FILE_PATH}.scala",
          "-sourcepath", baseDirectory.in(LocalRootProject).value.getAbsolutePath,
          "-diagrams"
        ),
        git.remoteRepo := gh.repo,
        includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.yml" | "*.md")
}

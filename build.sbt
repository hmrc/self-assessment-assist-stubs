import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / scalaVersion := "3.5.2"
ThisBuild / majorVersion := 0
ThisBuild / scalacOptions ++= Seq(
  "-Werror",
  "-Wconf:msg=Flag.*repeatedly:s"
)

val appName = "self-assessment-assist-stubs"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scalacOptions ++= Seq(
      "-feature",
      "-Wconf:src=routes/.*:s"
    )
  )
  .settings(
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
    Compile / unmanagedClasspath += baseDirectory.value / "resources"
  )
  .settings(CodeCoverageSettings.settings)
  .settings(PlayKeys.playDefaultPort := 8343)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(
    Test / fork := true)
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings}

val appName = "self-assessment-assist-stubs"

lazy val ItTest = config("it") extend Test

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    majorVersion                     := 0,
    scalaVersion                     := "2.13.16",
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    scalacOptions ++= List(
      "-language:higherKinds",
      "-Xlint:-byname-implicit",
      "-Xfatal-warnings",
      "-Wconf:src=routes/.*:silent",
      "-feature"
    )
  )
.settings(defaultSettings() *)
  .configs(ItTest)
  .settings(
    ItTest / fork                       := true,
    ItTest / unmanagedSourceDirectories := Seq((ItTest / baseDirectory).value / "it"),
    ItTest / unmanagedClasspath += baseDirectory.value / "resources",
    Runtime / unmanagedClasspath += baseDirectory.value / "resources",
    addTestReportOption(ItTest, "int-test-reports")
  )
  .settings(PlayKeys.playDefaultPort := 8343)

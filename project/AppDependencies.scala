import sbt.*

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % "10.4.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % "10.4.0",
    "org.scalamock"        %% "scalamock"               % "7.4.0",
  ).map(_ % Test)

}

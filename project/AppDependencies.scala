
import sbt.*

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % "9.13.0",
    "commons-codec" % "commons-codec" % "1.18.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % "9.13.0" % "test, it"
  )
}

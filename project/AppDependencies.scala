import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % "8.1.0",
    "commons-codec" % "commons-codec" % "1.15"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % "8.1.0" % "test, it"
  )
}

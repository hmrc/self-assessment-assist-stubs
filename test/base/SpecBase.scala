/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package base

/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite

abstract class SpecBase extends AnyWordSpec with Matchers with GuiceOneServerPerSuite with BeforeAndAfterEach

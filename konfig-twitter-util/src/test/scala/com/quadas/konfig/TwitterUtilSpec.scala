package com.quadas.konfig

import cats.syntax.validated._
import com.twitter.conversions.DurationOps._
import com.twitter.conversions.StorageUnitOps._
import com.twitter.util.{Duration, StorageUnit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpecLike}

import com.quadas.konfig.twitterutil._

class TwitterUtilSpec extends Matchers with WordSpecLike {
  "duration reader" should {
    "work" in {
      ConfigFactory.parseString("d = 300 ms").read[Duration]("d") should be(300.millis.validNel)
      ConfigFactory.parseString("d = 125 days").read[Duration]("d") should be(125.days.validNel)
    }
  }

  "storage unit reader" should {
    "work" in {
      ConfigFactory.parseString("s = 876 k").read[StorageUnit]("s") should be(876.kilobytes.validNel)
      ConfigFactory.parseString("s = 50 g").read[StorageUnit]("s") should be(50.gigabytes.validNel)
    }
  }
}

package konfig

import cats.syntax.validated._
import com.twitter.util.{Duration, StorageUnit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpecLike}

import konfig.twitterutil._

class TwitterUtilSpec extends Matchers with WordSpecLike {
  "duration reader" should {
    "work" in {
      import com.twitter.conversions.DurationOps._
      ConfigFactory.parseString("d = 300 ms").read[Duration]("d") should be(300L.millis.validNec)
      ConfigFactory.parseString("d = 125 days").read[Duration]("d") should be(125L.days.validNec)
    }
  }

  "storage unit reader" should {
    "work" in {
      import com.twitter.conversions.StorageUnitOps._
      ConfigFactory.parseString("s = 876 k").read[StorageUnit]("s") should be(876L.kilobyte.validNec)
      ConfigFactory.parseString("s = 50 g").read[StorageUnit]("s") should be(50L.gigabytes.validNec)
    }
  }
}

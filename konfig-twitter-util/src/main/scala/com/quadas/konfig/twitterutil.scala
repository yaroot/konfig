package com.quadas.konfig

import cats.syntax.validated._
import com.twitter.util.{Duration, StorageUnit}
import com.typesafe.config.Config

trait TwitterUtilReaders {
  implicit val durationReader: ConfigReader[Duration] = (c: Config, path: String) => {
    Duration.fromNanoseconds(c.getDuration(path).toNanos).validNel
  }

  implicit val storageUnitReader: ConfigReader[StorageUnit] = (c: Config, path: String) => {
    StorageUnit.fromBytes(c.getBytes(path)).validNel
  }
}

package object twitterutil extends TwitterUtilReaders

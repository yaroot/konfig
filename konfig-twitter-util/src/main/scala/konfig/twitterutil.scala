package konfig

import cats.syntax.validated._
import com.twitter.util.{Duration, StorageUnit}
import com.typesafe.config.Config

trait TwitterUtilReaders {
  implicit val durationReader: KonfigReader[Duration] = (c: Config, path: String) => {
    Duration.fromNanoseconds(c.getDuration(path).toNanos).validNel
  }

  implicit val storageUnitReader: KonfigReader[StorageUnit] = (c: Config, path: String) => {
    StorageUnit.fromBytes(c.getBytes(path)).validNel
  }
}

package object twitterutil extends TwitterUtilReaders

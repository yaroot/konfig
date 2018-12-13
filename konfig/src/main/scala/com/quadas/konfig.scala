import cats.data.ValidatedNel
import com.typesafe.config.Config

import scala.language.higherKinds

package object konfig extends ProductReaders with StandardReaders with DeriveKonfigReaders {
  type KonfigResult[A] = ValidatedNel[KonfigError, A]

  implicit val keyStyle: KeyStyle = KeyStyle.Hyphen

  implicit object subtypeHint extends SubtypeHint {
    override def fieldName(): String = "type"
    override def matchType(fieldValue: String, typeName: String): Boolean =
      typeName.toLowerCase().startsWith(fieldValue.toLowerCase())
  }

  implicit class EnrichedConfig(private val underlying: Config) extends AnyVal {
    def read[T](path: String)(implicit cr: KonfigReader[T]): KonfigResult[T] = {
      cr.read(underlying, path)
    }

    def read[T]()(implicit cr: KonfigReader[T]): KonfigResult[T] = {
      cr.read(underlying)
    }

    def withVals[T: ValueConverter](pairs: (String, T)*): Config = {
      pairs.foldRight(underlying) { (a, c) =>
        c.withValue(a._1, implicitly[ValueConverter[T]].toConfigValue(a._2))
      }
    }

    def without(path: String): Config = {
      underlying.withoutPath(path)
    }
  }
}

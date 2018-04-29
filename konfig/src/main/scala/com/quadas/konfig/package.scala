package com.quadas.konfig

import cats.Apply
import cats.instances.all._
import cats.syntax.traverse._
import cats.syntax.validated._
import com.typesafe.config.{Config, ConfigMemorySize, ConfigValue}
import shapeless._
import shapeless.labelled.{FieldType, field}

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.language.higherKinds
import scala.util.Try

// could be an exception, I'm lazy
sealed trait KonfigError {
  def cause: Throwable
  def exception(): Throwable = new RuntimeException(toString, cause)
  // TODO add useful combinators (like `map`)
}

object KonfigError {
  def apply(message: String) = WithoutCause(message)
  def apply(message: String, cause: Throwable) = WithCause(message, cause)
  def apply(cause: Throwable) = Cause(cause)

  case class WithCause(message: String, cause: Throwable) extends KonfigError
  case class WithoutCause(message: String) extends KonfigError { val cause: Null = null }
  case class Cause(cause: Throwable) extends KonfigError
}

object KonfigResult {
  def success[A](value: A): KonfigResult[A] = value.validNel
  def error[A](message: String): KonfigResult[A] = KonfigError(message).invalidNel
  def error[A](message: String, cause: Throwable): KonfigResult[A] = KonfigError(message, cause).invalidNel
  def error[A](cause: Throwable): KonfigResult[A] = KonfigError(cause).invalidNel
  def fromTry[A](_try: Try[A]): KonfigResult[A] = _try.fold(error[A](_), success)
  def fromTry[A](_try: Try[A], message: => String): KonfigResult[A] = _try.fold(error(message, _), success)
}

trait KonfigReader[T] {
  def read(c: Config, path: String): KonfigResult[T]
  def read(c: Config): KonfigResult[T] = {
    read(c.atKey("_"), "_")
  }
  def read(c: ConfigValue): KonfigResult[T] = {
    read(c.atKey("_"), "_")
  }
}

object KonfigReader {
  def of[T](f: (Config, String) => T): KonfigReader[T] =
    (c: Config, path: String) => KonfigResult.fromTry(Try(f(c, path)), s"Failed to read $path")

  def fromString[T](f: String => T): KonfigReader[T] =
    (c: Config, path: String) => KonfigResult.fromTry(Try(f(c.getString(path))), s"Failed to read $path")
}

/** Type hint for coproduct (sealed trait)
 * TODO make it more powerful
 */
trait SubtypeHint {
  def fieldName(): String
  def matchType(fieldValue: String, typeName: String): Boolean
}

/** path name translation */
trait KeyStyle {
  def style(key: String): String
}

object KeyStyle {
  object Same extends KeyStyle {
    override def style(key: String): String = key
  }

  // someVeryLongName -> some-very-long-name, ASCII SUPPORT ONLY
  object Hyphen extends KeyStyle {
    override def style(key: String): String = {
      "[A-Z]".r.replaceAllIn(key, "-" + _.matched.toLowerCase).stripPrefix("-")
    }
  }
}

trait ProductReaders {
  implicit val hNilReader: KonfigReader[HNil] = (_: Config, _: String) => KonfigResult.success(HNil)

  implicit def hListReader[Key <: Symbol, Head, Tail <: HList](
    implicit
    key: Witness.Aux[Key],
    keyStyle: KeyStyle,
    cr: Lazy[KonfigReader[Head]],
    tail: Lazy[KonfigReader[Tail]]
  ): KonfigReader[FieldType[Key, Head] :: Tail] = (c: Config, path: String) => {
    val value: KonfigResult[FieldType[Key, Head]] =
      cr.value
        .read(c.getConfig(path), keyStyle.style(key.value.name))
        .map(field[Key](_))
    val tailVal: KonfigResult[Tail] = tail.value.read(c, path)

    Apply[KonfigResult].map2(value, tailVal)(_ :: _)
  }

  implicit val cNilReader: KonfigReader[CNil] = (_: Config, _: String) =>
    KonfigResult.error("Can't find a valid choice from all subtypes")

  implicit def coproductReader[Key <: Symbol, Head, Tail <: Coproduct](
    implicit
    key: Witness.Aux[Key],
    subtypeHint: SubtypeHint,
    cr: Lazy[KonfigReader[Head]],
    tail: Lazy[KonfigReader[Tail]]
  ): KonfigReader[FieldType[Key, Head] :+: Tail] = (c: Config, path: String) => {
    val subtypeValue = c.getConfig(path).getString(subtypeHint.fieldName())
    if (subtypeHint.matchType(subtypeValue, key.value.name))
      cr.value.read(c, path).map(head => Inl(field[Key](head)))
    else
      tail.value.read(c, path).map(Inr(_))
  }

  implicit def productReader[T, Repr](
    implicit
    gen: LabelledGeneric.Aux[T, Repr],
    cr: Cached[Strict[KonfigReader[Repr]]]
  ): KonfigReader[T] = (c: Config, path: String) => {
    cr.value.value.read(c, path).map(gen.from)
  }
}

trait StandardReaders {
  implicit val stringReader: KonfigReader[String] = KonfigReader.fromString(identity)

  implicit val intReader: KonfigReader[Int] = KonfigReader.of(_.getInt(_))

  implicit val longReader: KonfigReader[Long] = KonfigReader.of(_.getLong(_))

  implicit val booleanReader: KonfigReader[Boolean] = KonfigReader.of(_.getBoolean(_))

  implicit val floatReader: KonfigReader[Float] = KonfigReader.of(_.getDouble(_).toFloat)

  implicit val doubleReader: KonfigReader[Double] = KonfigReader.of(_.getDouble(_))

  implicit val bigDecimalReader: KonfigReader[BigDecimal] = KonfigReader.fromString(BigDecimal.apply)

  implicit val finiteDurationReader: KonfigReader[FiniteDuration] = KonfigReader.of(_.getDuration(_).toNanos.nanos)

  implicit val memorySizeReader: KonfigReader[ConfigMemorySize] = KonfigReader.of(_.getMemorySize(_))

  implicit val configReader: KonfigReader[Config] = KonfigReader.of(_.getConfig(_))

  implicit def strMapReader[T](implicit cr: KonfigReader[T]): KonfigReader[Map[String, T]] = {
    (c: Config, path: String) =>
      {
        c.getConfig(path)
          .entrySet()
          .asScala
          .toVector // Vector[Map.Entry[?]]
          .map { ent =>
            cr.read(ent.getValue).map(ent.getKey -> _) // KonfigResult[(String, T)]
          }
          .sequence
          .map(_.toMap)
      }
  }

  implicit def vectorReader[T](
    implicit cr: KonfigReader[T]
  ): KonfigReader[Vector[T]] = (c: Config, path: String) => {
    val as: Vector[KonfigResult[T]] = c
      .getList(path)
      .asScala
      .toVector
      .map(cr.read)
    as.sequence
  }

  implicit def listReader[T: KonfigReader]: KonfigReader[List[T]] =
    (c: Config, path: String) => vectorReader[T].read(c, path).map(_.toList)

  implicit def setReader[T: KonfigReader]: KonfigReader[Set[T]] =
    (c: Config, path: String) => vectorReader[T].read(c, path).map(_.toSet)

  implicit def optionReader[T](implicit cr: KonfigReader[T]): KonfigReader[Option[T]] =
    (c: Config, path: String) => {
      if (c.hasPath(path)) {
        cr.read(c, path).map(Option.apply)
      } else KonfigResult.success[Option[T]](Option.empty[T])
    }
}

trait DeriveKonfigReaders {
  def deriveKonfigReader[T](implicit cr: Lazy[KonfigReader[T]]): KonfigReader[T] = cr.value
}

trait ValueConverter[T] {
  def toConfigValue(t: T): ConfigValue
}

object ValueConverter {
  import com.typesafe.config.ConfigValueFactory._
  def of[T](f: T => ConfigValue): ValueConverter[T] = (t: T) => f(t)

  implicit val int: ValueConverter[Int] = of[Int](a => fromAnyRef(a))
  implicit val long: ValueConverter[Long] = of[Long](a => fromAnyRef(a))
  implicit val float: ValueConverter[Float] = of[Float](a => fromAnyRef(a))
  implicit val double: ValueConverter[Double] = of[Double](a => fromAnyRef(a))
  implicit val string: ValueConverter[String] = of[String](a => fromAnyRef(a))

  implicit def option[T: ValueConverter]: ValueConverter[Option[T]] =
    (t: Option[T]) => {
      t match {
        case Some(t0) => implicitly[ValueConverter[T]].toConfigValue(t0)
        case _        => null
      }
    }

  implicit def map[V: ValueConverter]: ValueConverter[Map[String, V]] =
    (t: Map[String, V]) => {
      val valueMap = t.mapValues(implicitly[ValueConverter[V]].toConfigValue)
      fromMap(valueMap.asJava)
    }

  implicit def seq[T, C[_] <: Seq[T]](implicit vc: ValueConverter[T]): ValueConverter[C[T]] =
    (t: C[T]) => {
      fromIterable(t.toVector.map(vc.toConfigValue).asJava)
    }
}

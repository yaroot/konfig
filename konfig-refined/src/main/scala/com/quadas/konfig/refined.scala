package com.quadas.konfig

import com.typesafe.config.Config
import eu.timepit.refined.api.{RefType, Validate}
import shapeless.Lazy

trait RefinedReaders {
  implicit def refinedReader[T, P, F[_, _]](
    implicit
    reader: Lazy[KonfigReader[T]],
    validate: Validate[T, P],
    refType: RefType[F]
  ): KonfigReader[F[T, P]] = (c: Config, path: String) => {
    val result = reader.value.read(c, path)
    result.andThen { value =>
      KonfigResult.fromEither(refType.refine(value))(KonfigError(_))
    }
  }
}

package object refined extends RefinedReaders

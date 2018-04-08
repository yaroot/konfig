package com.quadas.konfig

import com.typesafe.config.{Config, ConfigException}
import eu.timepit.refined.api.{RefType, Validate}

trait RefinedReaders {
  implicit def refinedReader[T, P, F[_, _]](
    implicit
    reader: ConfigReader[T],
    validate: Validate[T, P],
    refType: RefType[F]
  ): ConfigReader[F[T, P]] = new ConfigReader[F[T, P]] {
    override def read(c: Config, path: String): F[T, P] = {
      val result = reader.read(c, path)
      refType.refine(result) match {
        case Left(err) => throw new ConfigException.Generic(s"$path: $err")
        case Right(x)  => x
      }
    }
  }
}

package object refined extends RefinedReaders

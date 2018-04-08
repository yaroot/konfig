package com.quadas.konfig

import com.typesafe.config.{ConfigException, ConfigFactory}
import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean._
import eu.timepit.refined.string._
import eu.timepit.refined.numeric._
import org.scalatest.{FlatSpec, Matchers}
import com.quadas.konfig._
import com.quadas.konfig.refined._
import shapeless.tag.@@

class RefinedSpec extends FlatSpec with Matchers {
  type PortNumber = GreaterEqual[W.`0`.T] And LessEqual[W.`65535`.T]
  case class Port(value: Int Refined PortNumber)

  "Refined Reader" should "work with `refineV`" in {
    ConfigFactory.parseString("value = 0").read[Port] should be(Port(refineMV[PortNumber](0)))
    ConfigFactory.parseString("value = 65535").read[Port] should be(Port(refineMV[PortNumber](65535)))
    an[ConfigException] should be thrownBy ConfigFactory.parseString("port { value = -1 }").read[Port]
  }

  type StartsWithA = StartsWith[W.`"a"`.T]
  "Refined Reader" should "work with `refineT`" in {
    ConfigFactory.parseString("foo = \"abc\"").read[String @@ StartsWithA]("foo") should be(
      refineMT[StartsWithA]("abc")
    )
    an[ConfigException] should be thrownBy ConfigFactory.parseString("foo = \"bar\"").read[String @@ StartsWithA]("foo")
  }

}

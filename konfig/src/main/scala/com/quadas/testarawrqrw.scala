//package com.quadas
//
//import magnolia._
//import scala.language.experimental.macros
//
//sealed trait Message
//case class Tweet(user: String, text: String) extends Message
//case class Event(typ: String) extends Message
//
//trait Printer[X] {
//  def print(x: X): String
//}
//
//object Printer {
//  implicit val stringPrinter: Printer[String] = identity _
//
//  type Typeclass[T] = Printer[T]
//
//  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T] = {
//    v: T =>
//      {
//        if (caseClass.isObject) {
//          caseClass.typeName
//        } else {
//          caseClass.parameters
//            .map { c =>
//              c.typeclass.print(c.dereference(v))
//            }
//            .mkString(", ")
//        }
//      }
//  }
//
//  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] = {
//    v: T => {
//      sealedTrait.dispatch(v) { st =>
//        val vv = st.typeclass.print(st.cast(v))
//        s"${st.label}:vv"
//      }
//    }
//  }
//
//  implicit def gen[T]: Typeclass[T] = macro Magnolia.gen[T]
//}
//
//object testarawrqrw {
//
//  def print[A](a: A)(implicit printer: Printer[A]): Unit = {
//    println(printer.print(a))
//  }
//
//  def main(args: Array[String]): Unit = {
//    print(implicitly[Printer[String]].print("aaa"))
//    print(Tweet("bbb", "ccc"))
//    print(Event("follow"))
//    print(Event("unfollow"): Message)
//  }
//
//}


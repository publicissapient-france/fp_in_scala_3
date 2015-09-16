package fr.xebia.xke.fp3

sealed trait Retry[+T] {

  //TODO EXO7
  def flatMap[B](f: (T => Retry[B])): Retry[B] = ???

  //TODO EXO7
  def map[B](f: (T => B)): Retry[B] = ???

  //TODO EXO7
  def filter(predicate: (T => Boolean)): Retry[T] = ???

}

case class Failure(t: Throwable) extends Retry[Nothing]

case class Success[T](t: T, tries: Int = 0) extends Retry[T]

object Retry {

  //TODO EXO5
  def apply[T](retries: Int, tries: Int = 0)(t: () => T): Retry[T] = ???

}
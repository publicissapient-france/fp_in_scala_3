package fr.xebia.xke

sealed trait List[+A]

case object Nil extends List[Nothing]

case class Cons[A](a: A, t: List[A]) extends List[A]

object List{
  def apply[A](elts: A*): List[A] = if (elts.isEmpty) Nil else Cons(elts.head, List.apply(elts.tail: _*))

}

//OPTION
sealed trait Option[+A]

case object None extends Option[Nothing]

case class Some[A](a: A) extends Option[A]
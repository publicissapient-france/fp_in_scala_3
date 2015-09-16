# Monade


	@TODO


## Petit rappel

Lors de la première séance précédente, nous avions créée un type **List** et **Option** avec plusieurs méthodes.

	//LIST
	sealed trait List[+A]

	case object Nil extends List[Nothing]

	case class Cons[A](a: A, t: List[A]) extends List[A]


	//OPTION
	sealed trait Option[+A]

	case object None extends Option[Nothing]

	case class Some[A](a:A) extends Option[A]


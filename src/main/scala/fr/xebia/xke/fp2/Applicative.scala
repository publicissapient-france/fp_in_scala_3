package fr.xebia.xke.fp2

import fr.xebia.xke._

import scala.language.higherKinds

trait Applicative[F[_]] extends Foncteur[F] {

  def point[A](a: A): F[A]

  def ap[A, B](fa: F[A])(f: F[A => B]): F[B]

  override def map[A, B](fa: F[A])(f: (A => B)): F[B] = ap(fa)(point(f))

  def ap2[A, B, C](fa: F[A], fb: F[B])(f: F[(A, B) => C]): F[C] = {
    val curriedF: F[A => B => C] = map(f)(_.curried)

    val fAppliedOnFA: F[(B) => C] = ap(fa)(curriedF)
    
    ap(fb)(fAppliedOnFA)
  }
  
  def apply2[A, B, C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C] = ap2(fa, fb)(point(f))

  def mapply2[A, B, C](a: A, b: B)(f: F[(A, B) => C]): F[C] = ap2(point(a), point(b))(f)
  
}

object Applicative{

  val listApplicative = new Applicative[List] {

    override def point[A](a: A): List[A] = Cons(a, Nil)

    override def ap[A, B](fa: List[A])(f: List[(A) => B]): List[B] = {

      def loop(itFA: List[A])(itF: List[A => B]): List[B] = itF match {
        case Cons(func, tailF) =>
          itFA match {
            case Cons(a, tailA) =>
              Cons(func(a), loop(tailA)(itF))

            case Nil =>
              loop(fa)(tailF)

          }

        case Nil =>
          Nil
      }

      loop(fa)(f)
    }

  }

  val optionApplicative = new Applicative[Option] {

    override def point[A](a: A): Option[A] = Some(a)

    override def ap[A, B](fa: Option[A])(f: Option[(A) => B]): Option[B] = fa match {
      case Some(a) =>
        f match {
          case Some(func) =>
            Some(func(a))
          case None =>
            None
        }

      case None =>
        None
    }

  }
  
}
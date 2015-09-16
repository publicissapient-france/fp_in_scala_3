package fr.xebia.xke.fp2

import scala.language.higherKinds
import fr.xebia.xke._

trait Foncteur[F[_]] {
  
  def map[A, B](fa: F[A])(f: (A => B)): F[B]

  def fproduct[A, B](fa: F[A])(f: A => B): F[(A, B)] = map(fa)(a => (a, f(a)))


  def fpair[A, B](fa: F[A]): F[(A, A)] = map(fa)(a => (a, a))


  def mapply[A, B](a: A)(f: F[A => B]): F[B] = map(f) { (g: (A => B)) => g(a)}


  def lift[A, B](f: A => B): F[A] => F[B] = (fa: F[A]) => map(fa)(f)
  
}

object Foncteur{

  val listFoncteur = new Foncteur[List] {
    def map[A, B](fa: List[A])(f: (A => B)): List[B] = fa match {
      case Nil => Nil
      case Cons(x, t) => Cons(f(x), map(t)(f))
    }
  }

  val optionFoncteur = new Foncteur[Option] {
    def map[A, B](fa: Option[A])(f: (A => B)): Option[B] = fa match {
      case None => None
      case Some(x) => Some(f(x))
    }
  }
  
}
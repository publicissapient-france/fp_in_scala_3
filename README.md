# Monade

6 lettres et beaucoup de mystères. La monade est souvent le terme le plus entendu quand on parle de pattern en programmation fonctionnelle. 
Elle donne du fil à retordre au programmeur qui tente de la comprendre. Nous allons voir ensemble qu'il n'y a rien de mystérieux. 
La monade est au contraire un design pattern puissant pour construire des API élégantes et simples à utiliser.


## Petit rappel

Nous avons déjà abordé ensemble :

* la création de types simples en Scala, Option et List
	
Code
	
	//LIST
	sealed trait List[+A]

	case object Nil extends List[Nothing]

	case class Cons[A](a: A, t: List[A]) extends List[A]


	//OPTION
	sealed trait Option[+A]

	case object None extends Option[Nothing]

	case class Some[A](a:A) extends Option[A]

	

* le parcours purement fonctionnel d'une structure récursive

Code

	def printList[A](listA: List[A]): Unit = listA match {
	
	  case Nil => 
	  	Nil
	  	
	  case Cons(a, tail) => 
	  	println(a)
	  	printList(tail)
	  	
	}

* l'abstraction foncteur

Cette abstraction nécessite l'implémentation de la fonction **map** qui applique une fonction sur l'ensemble des valeurs contenu par le type.

En échange de cette seule méthode abstraite, l'abstraction nous permet par exemple d'appliquer un ensemble de fonctions sur une seule valeur 

	//Exemple d'utilisation
	map(List(1,2,3)(x => x +1)
	
	mapply(1)(List(x => x + 2, x => x * 4))
	
	
La somme d'entiers est une opération qui nécessite deux paramètres, ici on doit en figer un des deux (2 pour l'addition et 4 pour la multiplication).

Mais impossible avec le foncteur de passer une fonction du style ( x:Int , y:Int ) => x + y .


* l'abstraction applicative foncteur.

L'applicative est une spécialisation de foncteur. Elle a besoin de deux méthodes abstraites **point** et **ap**.
Grâce à cela, on peut non seulement avoir les mêmes API que le foncteur et bien d'autres encore. Le foncteur est limité à appliquer une fonction sur un ensemble d'éléments ou un ensemble de fonctions sur un seul élément. 
L'applicative lève cette contrainte en permettant l'application d'un ensemble de fonctions sur un ensemble d'éléments!

Par exemple, nous étions limités à appliquer + 1 sur une liste d'entiers, mais il était impossible avec le seul foncteur de faire la somme de deux listes d'entiers.

	//Exemple d'utilisation
	ap2(List(1,2), List(1,2))(List(
        (x:Int,y:Int) => x + y,
        (x:Int,y:Int) => x * y
      ))

	result_of_ap shouldBe List(
		2, // SUM
		3,
		
		3, // SUM
		4,
		
		1, // PRODUCT
		2,
		
		2, // PRODUCT
		4,
	)
	
Mais l'applicative a lui aussi quelques limitations. Regarder l'exemple suivant
 	
	val x = List("123","456")
 	
	val listOfChars = map(x)(str => List(str.toCharArray))
 	
	listOfChars shouldBe List(List('1','2','3'),List('4','5','6'))
 	
Si on s'intéresse au signature, nous avons

	x: List[String]
	
	f: String => List[Char]
	
	map(F[A])(A => B): F[B]
	
	//par substitution
	map(List[String])(String => List[Char]): List[List[Char]]
	
On remarque en sortie la répétition du type List. C'est un pattern! Voici la signature de ce que nous allons développer:

	x: List[String]
    	
	f: String => List[Char]
    	
	flatMap(F[A])(A => F[B]): F[B]
    	
    //par substitution
    flatMap(List[String])(String => List[Char]): List[Char]

En avant!


##Exo1: Monade de List
 
Jetez un œil au trait **fr.xebia.xke.fp3.Monade**. Elle contient au final deux méthodes abstraites, **point** et **flatMap**.

Créer l'instance de Monade dans l'objet compagnon **Monade** pour List.

Vous pouvez lancer les tests dans sbt avec

	sbt exo1


##Exo2: Monade extends Applicative

Écrivez **map** et **ap** en fonction de **flatMap** et **point** dans le trait Monade.

Tous les tests taggés EXO_2 de la classe **fr.xebia.xke.fp3.ListMonadeSpec** sont maitenant verts!

Vous pouvez lancer les tests dans sbt avec

	sbt exo2


##Exo3: flatten the monad

Écrivez **flatten** dans le trait Monade.

Tous les tests taggés EXO_3 de la classe **fr.xebia.xke.fp3.ListMonadeSpec** sont maitenant verts!

Vous pouvez lancer les tests dans sbt avec

	sbt exo3

##Exo4: monad laws

La monade repose elle aussi sur une structure mathématique. Il y a donc des propriétés à vérifier:

* right identity

Soit

	fa: F[A] 
	
	fa shouldBe flatMap(fa)(a => point(a))

* left identity

Soit 

	f: (A => F[B]) 
	a: A 
	
	f(a) shouldBe flatMap(point(a)(f)


* associative flatMap

Soit

	fa: F[A] 
	f: A => F[B]
	g: B => F[C]
	
	val result_of_flatMap_of_flatMap = flatMap(flatMap(fa)(f))(g)
	val result_of_nested_flatMap     = flatMap(fa)((a: A) => flatMap(f(a))(g))
	 
	result_of_flatMap_of_flatMap shouldBe result_of_nested_flatMap


Vous pouvez lancer les tests dans sbt avec

	sbt exo4

##Exo5: Retry

Il est temps de mettre tout cela en pratique. Il existe des tas de types qui pourraient avoir un comportement monadique.
Nous allons créer le notre, **Retry**. **Retry** est une structure qui permet d'exécuter plusieurs fois un morceau de code et d'encapsuler le retour.
Voici les types que vous pouvez retrouver dans **fr/xebia/xke/fp3/Retry.scala**.

	sealed trait Retry[+T]

	case class Failure(t: Throwable) extends Retry[Nothing]

	case class Success[T](t: T, tries: Int) extends Retry[T]
	
	object Retry{
	  def apply[T](retries: Int, tries: Int = 0)(t: () => T): Retry[T] = ???
	}

Évaluer une expression avec **Retry.apply** retourne **Success** avec la dernière valeur retournée dans le cas d'un succès, ou **Failure** avec la dernière expection si l'expression en génère une.

Implémenter maintenant la méthode **Retry.apply**.

Vous pouvez lancer les tests dans sbt avec

	sbt exo5

##Exo6: Retry the monad

Nous allons maintenant implémenter dans l'objet compagnon de **Monade** l'instance pour **Retry**. Implémenter la méthode **point** et **flatMap**

Vous pouvez lancer les tests dans sbt avec

	sbt exo6
	
##Exo7: Monads and for comprehension, the happy path

Maintenant que tout cela est en place, quels sont les avantages pour écrire du code. Reprenons l'exemple d'une méthode parseInt.

	def parseInt(s:String):Int
	
En Java, cette méthode fait un throw implicite de **java.text.ParseException**, car c'est une RuntimeException. Du coup, rien ne signifie que le traitement peut échouer.
 
 	def parseInt(s:String):Int throws ParseException
 	
Avec cette signature, on expose publiquement l'erreur possible. En approche fonctionnelle, on préfère que tout soit explicite aussi. Mais les exceptions sont trop liées à un concept de pile d'appel synchrone. On préfère toujours tout symboliser par une abstraction et des types.

	def parseInt(s:String):Retry[Int]
	
Si on veut parser deux éléments et les sommer, on s'attend à 

	try{
		val x = parseInt("1")
		val y = parseInt("2")
		x + y
	}
	catch {
		case e=>
	} ...
	
Par contre, avec la signature typée en Retry[Int], ce n'est pas possible. Du coup, la seule façon de le faire est:


	val k:Retry[Int] = flatMap(Retry(tries = 1 , () => parseInt("1"))){i => 
		map(Retry(tries = 1 , () => parseInt("2")){j => 
			i + j
		 }
	 }

Si maitenant on encapsule le **parseInt** de pour qu'il renvoie un **Retry[Int]**, on a :
 	
 	def retryParseInt(s:String): Retry[Int] = Retry(tries = 1)(() => parseInt(s))
 	
 	val k:Retry[Int] = flatMap(retryParseInt("1")){i => 
 		map(retryParseInt("2")){j => 
 			i + j
 		}
	}
	
C'est un peu mieux mais encore lourd. Scala permet d'écrire cela sous forme de **for comprehesion**. Le compilateur transforme l'expression **for** en combinaison de **map** et **flatMap**.
	
	val k: Retry[Int] = for{
		i <- retryParseInt("1")
		j <- retryParseInt("2")
	} yield i + j
	
Pour cela, il faut changer une chose sur le trait **Retry**. Il faut implémenter la méthode **map** et **flatMap** directement dessus.

	sealed trait Retry[+T] {

	  def flatMap[B](f: (T => Retry[B])): Retry[B] = Monade.retryMonade.flatMap(this)(f)

	  def map[B](f: (T => B)): Retry[B] = Monade.retryMonade.map(this)(f)

	}

Le for comprehension supporte aussi deux autres méthodes, **filter** et **foreach**.

**filter** permet de filter l'exécution d'un programme en fonction d'une condition sur une valeur de Success.

Implémenter filter sur le trait Retry.

	def filter(predicate: (T => Boolean)): Retry[T]
	
Ensuite, il est possible d'écrire ceci

	val k: Retry[Int] = for{
		i <- retryParseInt("1") if i < 1
		j <- retryParseInt("2")
	} yield i + j


**foreach** permet quant à lui d'exécuter des effets de bords quand le résultat du yield n'est pas intéressant, genre **println**.

Vous pouvez décommenter le code des tests de l'exercice 7 et lancer les tests dans sbt avec

	sbt exo7


##Exo8: Retry monads law

Si Retry a une instance de Monade, cette dernière doit respecter les lois associées. Décommenter les tests de l'exo 8 et lancer les tests dans sbt avec

	sbt exo8

Que remarquez vous sur le dernier? Qu'en concluez-vous ?

##Exo9: Monad, Applicative, Functor?

Nous voici donc avec 3 abstractions. Mais comment choisir? 

Petit conseil, choisissez toujours l'abstraction la plus simple. Nous avons vu que pour passer d'un foncteur à un applicative et d'un applicative à une monade, il y a des propriétés suplémentaire à ajoute des lois et contraintes que le type et son opération de composition doit respecter.

Commencez donc par utiliser un Foncteur si map suffit, vous ferez le choix d'un applicative quand vos structures doivent être manipulées 2 par 2, et une monade pour chaîner des traitements.

La monade est elle vraiment plus puissante que les deux autres? Ses contraintes l'empêches d'une seule chose que les deux autres ont automatiquement, la composition.

En effet, foncteurs et applicatives se composent entre eux automatiquement, pas les monades.

Rendez-vous dans les classes **fr.xebia.xke.fp2.ListApplicativeSpec** et **fr.xebia.xke.fp2.ListeFoncteurSpec** puis regardez les tests EXO_3_9. 

Dans le cadre du foncteur, nous avons déjà créé un foncteur d'Option et un foncteur de List. Nous les composons pour créer un foncteur de List d'Option.

      val plus_one: (Int => Int) = _ + 1

 	  //composition du foncteur list puis option
      val list_of_options = List(Some(1), None)
      val listOptionFoncteur = Foncteur.listFoncteur.compose(Foncteur.optionFoncteur)
      val firstResult = listOptionFoncteur.map(list_of_options)(plus_one)
      firstResult shouldBe List(Some(2), None)

      //composition du foncteur option puis list
      val option_of_list = Some(List(1, 2, 3))
      val optionListFoncteur = Foncteur.optionFoncteur.compose(Foncteur.listFoncteur)
      val secondResult = optionListFoncteur.map(option_of_list)(plus_one)
      secondResult shouldBe Some(List(2, 3, 4))

      //composition du foncteur list puis list
      val list_of_list = List(List(1, 2, 3), Nil)
      val listListFoncteur = Foncteur.listFoncteur.compose(Foncteur.listFoncteur)
      val thirdResult = listListFoncteur.map(list_of_list)(plus_one)
      secondResult shouldBe List(List(2, 3, 4), Nil)

Ce qui est intéressant et puissant, c'est d'avoir écrit une seule fois l'instance de foncteur de list et d'option puis ensuite de composer ses instances pour être capable d'appliquer cette fonction **plus_one** dans plusieurs contexte.

On peut ainsi application **plus_one** sur une List[Option], une Option[List] ou encore List[List].

Cela est aussi vrai pour les applicatives.

      val plus: ((Int, Int) => Int) = _ + _

      //composition de l'applicative list puis option
      val list_of_options_1 = List(Some(1), None)
      val list_of_options_2 = List(None, Some(2))
      val listOptionApplicative = Applicative.listApplicative.compose(Applicative.optionApplicative)
      val firstResult = listOptionApplicative.apply2(list_of_options_1, list_of_options_2)(plus)
      firstResult shouldBe List(None, Some(3), None, None)

	  //composition de l'applicative option puis list
      val option_of_list1 = Some(List(1, 2, 3))
      val option_of_list2 = None
      val optionListApplicative = Applicative.optionApplicative.compose(Applicative.listApplicative)
      val secondResult = optionListApplicative.apply2(option_of_list1, option_of_list2)(plus)
      secondResult shouldBe None

      //composition de l'applicative list puis list
      val list_of_list_1 = List(List(1), Nil)
      val list_of_list_2 = List(Nil, List(2, 3))
      val listListApplicative = Applicative.listApplicative.compose(Applicative.listApplicative)
      val thirdResult: List[List[Int]] = listListApplicative.apply2(list_of_list_1, list_of_list_2)(plus)
      thirdResult shouldBe List(
        Nil,
        List(3, 4),
        Nil,
        Nil
      )

**compose** est une méthode puissante générique pour TOUTES les instances de foncteur et applicative.

Nous ne le ferons pas ici mais pour une monade, il est nécessaire d'écrire la méthode compose pour chaque combinaison!
L'instance Monad[Option[List[_]] est différente de celle Monad[List[Option[_]]. D'ailleurs, il existe une monade pour combiner les monades! Mais cela dépasse le cadre de cet atelier.
	
##Exo10: Another monad, the writer

Et voici un dernier exercice pour se mettre en jambe. Nous allons écrire la monade Writer. 

Celle-ci permet d'encapsuler l'écriture d'une "log" avec l'application d'une fonction. 

Nous allons créé ici une instance de monade Writer qui maintient une chaine de caractères des différentes opérations effectuées sur la monade. Nous l'appelons **StringWriter**.

C'est donc une *case class* qui contient une valeur et la chaîne de caractères de log. Il existe deux méthodes de constructions sur l'objet compagnon, **startWith** et **alterTo**. 
Ce sont simplément des constructeurs qui apportent de la sémantique à la lecture du code.

Implémenter l'instance de monade de StringWriter.
	
	case class StringWriter[A](value: A, log: String) {

	  def map[B](f: A => B): StringWriter[B] = StringWriter.stringWriterMonad.map(this)(f)

	  def flatMap[B](f: A => StringWriter[B]): StringWriter[B] = StringWriter.stringWriterMonad.flatMap(this)(f)

	}

	object StringWriter {

	  private def now = Instant.now()

	  def startWith[A](a: A): StringWriter[A] = apply(a)

	  def alterTo[A](newValue: A, cause: String): StringWriter[A] = StringWriter(newValue, s"$now: changing to: $newValue | ** $cause")

	  lazy val stringWriterMonad = new Monade[StringWriter] {

		override def flatMap[A, B](a: StringWriter[A])(f: (A) => StringWriter[B]): StringWriter[B] = ???

		override def point[A](a: A): StringWriter[A] = ???
	  }
	}

Implémenter l'instance de monade de JsonWriter, qui fait presque la même chose mais log les événements dans un JSArray.

TO JSON Writer

	case class JsonWriter[A](value: A, log: JsArray) {

	  def map[B](f: A => B): JsonWriter[B] = JsonWriter.jsonWriterMonad.map(this)(f)

	  def flatMap[B](f: A => JsonWriter[B]): JsonWriter[B] = JsonWriter.jsonWriterMonad.flatMap(this)(f)

	}


	object JsonWriter {

	  private def now = Instant.now()

	  def startWith[A](a: A): JsonWriter[A] = apply(a)

	  def alterTo[A](newValue: A, cause: String): JsonWriter[A] = JsonWriter(newValue, JsArray(
		Seq(Json.obj(
		  "time" -> now.toString,
		  "value" -> newValue.toString,
		  "message" -> s"replacing value"))
	  ))


	  lazy val jsonWriterMonad = new Monade[JsonWriter] {

		override def flatMap[A, B](a: JsonWriter[A])(f: (A) => JsonWriter[B]): JsonWriter[B] = ???

		override def point[A](a: A): JsonWriter[A] = ???
	  }
	}
	
Quels sont les points communs entre les deux implémentations? Les différences?

Essayer de créer 1 seul (classe|trait) avec la signature suivante 
	
	Writer[Log,A](value:A,log:Log)	
	
	
	
Nous verrons par la suite qu'il y a une contrainte sur le type **Log**. Rendez-vous au prochain épisode pour en savoir plus ;-)
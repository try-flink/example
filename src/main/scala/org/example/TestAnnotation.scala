package org.example

import scala.annotation.StaticAnnotation
import scala.reflect.runtime.universe._

object TestAnnotation {
  def main(args: Array[String]): Unit = {
    val tpe: Type = typeOf[MyTest]
    val symbol: Symbol = tpe.typeSymbol
    val annotation: Annotation = symbol.annotations.head
    val tree: Tree = annotation.tree

    println(showRaw(tree))

    val Apply(_, Literal(Constant(a: Int)) :: Nil) = tree
    println(s"Annotation args: name -> $a")
  }
}

class Test(var a: Int) extends StaticAnnotation {}

@Test(a = 3)
class MyTest {}
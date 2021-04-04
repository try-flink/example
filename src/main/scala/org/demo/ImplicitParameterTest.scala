package org.demo

abstract class Monoid[A] {
  def add(x: A, y: A): A

  def unit: A
}

/**
 * scala 隐式参数
 */
object ImplicitParameterTest {
  implicit val StringMonoid: Monoid[String] = new Monoid[String] {
    override def add(x: String, y: String): String = x concat (y)

    override def unit: String = ""
  }

  // 所以不用试Int开头，推导从类型中获取？
  implicit val ITntMonoid: Monoid[Int] = new Monoid[Int] {
    override def add(x: Int, y: Int): Int = x + y

    override def unit: Int = 0
  }

  def sum[A](xs: List[A])(implicit m: Monoid[A]): A =
    if (xs.isEmpty) m.unit
    else m.add(xs.head, sum(xs.tail))

  def main(args: Array[String]): Unit = {
    println(sum(List(1, 2, 3))) // uses intMonoid implicitly
    println(sum(List("a", "b", "c"))) // uses stringMonoid implicitly
  }
}

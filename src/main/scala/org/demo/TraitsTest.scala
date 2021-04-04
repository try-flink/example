package org.demo

import scala.collection.mutable.ArrayBuffer

object TraitsTest {
  def main(args: Array[String]): Unit = {
    val iterator = new IntIterator(10)
    println(iterator.next(), iterator.next())

    val animals = ArrayBuffer.empty[Pet]
    animals.append(new Dog("Harry"))
    animals.append(new Cat("Sally"))

    animals.foreach(pet => println(pet.name))
  }
}


trait Iterator[A] {
  def hasNext: Boolean

  def next(): A
}

class IntIterator(to: Int) extends Iterator[Int] {
  private var current = 0

  override def hasNext: Boolean = current < to

  override def next(): Int = {
    if (hasNext) {
      val t = current
      current += 1
      t
    } else 0
  }
}


trait Pet {
  val name: String
}

class Dog(val name: String) extends Pet
class Cat(val name: String) extends Pet
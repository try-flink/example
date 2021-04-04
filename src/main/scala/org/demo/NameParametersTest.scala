package org.demo

object NameParametersTest {
  def main(args: Array[String]): Unit = {
    println(eval(30))

    var i = 2
    whileLoop(i > 0) {
      println(i)
      i -= 1
    }

    test({
      println(1)
      println(2)
    })

    testReturn({
      1
    })

    println(testFuncBody(0) {
      println("this is testFuncBody(0) equal 0")
      3
    })

    println(testFuncBody(1) {
      println("this is testFuncBody(3) equal 3")
      3
    })


  }

  def eval(input: => Int) = input * 3

  def whileLoop(condition: => Boolean)(body: => Unit): Unit = {
    if (condition) {
      body
      whileLoop(condition)(body)
    }
  }

  def test(t: => Unit): Unit = {
    t
  }

  def testReturn(t: => Int): Unit = {
    println(t)
  }

  def testFuncBody(t: => Int)(body: => Int): Int = {
    if (t > 0) body
    else 0
  }
}

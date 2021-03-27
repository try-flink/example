package org.example

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time

object WindowJoin {

  case class Grade(name: String, grade: Int)

  case class Salary(name: String, salary: Int)

  case class Person(name: String, grade: Int, salary: Int)

  def main(args: Array[String]): Unit = {
    val params = ParameterTool.fromArgs(args)
    val windowSize = params.getLong("windowSize", 2000)
    val rate = params.getLong("rate", 3)

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime)

    val grades = WindowJoinSampleData.getGradeSource(env, rate)
    val salaries = WindowJoinSampleData.getSalarySource(env, rate)

    val joined = joinStreams(grades, salaries, windowSize)
    joined.print().setParallelism(1)

    env.execute("Windowed Join Example")
  }

  def joinStreams(
                   grades: DataStream[Grade],
                   salaries: DataStream[Salary],
                   windowSize: Long): DataStream[Person] = {
    grades.join(salaries)
      .where(_.name)
      .equalTo(_.name)
      .window(TumblingEventTimeWindows.of(Time.milliseconds(windowSize)))
      .apply( (g, s) => Person(g.name, g.grade, s.salary))
  }
}

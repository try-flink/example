package org.example

import org.apache.flink.api.scala._
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala._


object WordCountSQL {
  def main(args: Array[String]): Unit = {
    // 安装可执行环境
    val env = ExecutionEnvironment.getExecutionEnvironment
    val tEnv = BatchTableEnvironment.create(env)

    val input = env.fromElements(WC("hello", 1), WC("hello", 1), WC("ciao", 1))

    // 注册DataSet为视图
    tEnv.createTemporaryView("WordCount", input, $"word", $"frequency")

    tEnv.createTemporarySystemFunction("sub_string", classOf[TestFunc])

    // SQL query, retrieve 结果返回一个新的表
    val table = tEnv.sqlQuery("SELECT word, SUM(frequency) FROM WordCount GROUP BY word")

    table.toDataSet[WC].print()

    // test udf
    val b = tEnv.sqlQuery("SELECT sub_string(word, 1, 2), frequency FROM WordCount")

    b.toDataSet[WC].print()
  }

  case class WC(word: String, frequency: Long)
}

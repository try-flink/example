package org.example

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.assigners.EventTimeSessionWindows
import org.apache.flink.streaming.api.windowing.time.Time

object SessionWindowing {
  def main(args: Array[String]): Unit = {

    val params = ParameterTool.fromArgs(args)
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.getConfig.setGlobalJobParameters(params)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.setParallelism(1)

    val input = List(
      ("a", 1L, 1),
      ("b", 1L, 1),
      ("b", 3L, 1),
      ("b", 5L, 1),
      ("c", 6L, 1),
      // We expect to detect the session "a" earlier than this point (the old
      // functionality can only detect here when the next starts)
      ("a", 10L, 1),
      // We expect to detect session "b" and "c" at this point as well
      ("c", 11L, 1)
    )

    // output
    // 时间空隙是3
    // (a,1,1)
    // (b,1,3)
    // (c,6,1)
    // (a,10,1)
    // (c,11,1)

    // 定义source
    val source: DataStream[(String, Long, Int)] = env.addSource(
      new SourceFunction[(String, Long, Int)] {
        override def run(sourceContext: SourceFunction.SourceContext[(String, Long, Int)]): Unit = {
          input.foreach(value => {
            sourceContext.collectWithTimestamp(value, value._2)
            sourceContext.emitWatermark(new Watermark(value._2 - 1))
          })
          sourceContext.emitWatermark(new Watermark(Long.MaxValue))
        }

        override def cancel(): Unit = {}
      }
    )

    // 每个id的最大超时为3个时间单位
    val aggregated: DataStream[(String, Long, Int)] = source
      .keyBy(_._1)
      .window(EventTimeSessionWindows.withGap(Time.milliseconds(3L)))
      .sum(2)

    aggregated.print()
    env.execute("Session Winding")
  }
}

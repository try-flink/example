package org.example

import java.beans.Transient
import java.util.concurrent.TimeUnit

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.functions.windowing.delta.DeltaFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows
import org.apache.flink.streaming.api.windowing.evictors.TimeEvictor
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.triggers.DeltaTrigger

import scala.language.postfixOps
import scala.util.Random

object TopSpeedWindowing {
  val numOfCars = 2
  val evictionSec = 10
  val triggerMeters = 50d

  def main(args: Array[String]): Unit = {
    val params = ParameterTool.fromArgs(args)

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.getConfig.setGlobalJobParameters(params)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.setParallelism(1)

    val cars = env.addSource(new CarSource())

    val topSpeeds = cars
      .assignAscendingTimestamps( _.time )
      .keyBy(_.cardId)
      .window(GlobalWindows.create)
      .evictor(TimeEvictor.of(Time.of(evictionSec * 1000, TimeUnit.MILLISECONDS)))
      .trigger(DeltaTrigger.of(triggerMeters, new DeltaFunction[CarEvent] {
        def getDelta(oldSp: CarEvent, newSp: CarEvent): Double = newSp.distance - oldSp.distance
      }, cars.getType().createSerializer(env.getConfig)))
      //      .window(Time.of(evictionSec * 1000, (car : CarEvent) => car.time))
      //      .every(Delta.of[CarEvent](triggerMeters,
      //          (oldSp,newSp) => newSp.distance-oldSp.distance, CarEvent(0,0,0,0)))
      .maxBy("speed")

    topSpeeds.print()
    env.execute("TopSpeedWindowing")
  }
}

case class CarEvent(cardId: Int, speed: Int, distance: Double, time: Long)

class CarSource extends SourceFunction[CarEvent] {

  var isRunning: Boolean = true

  val speeds: Array[Integer] = Array.fill[Integer](2)(50)
  val distances: Array[Double] = Array.fill[Double](2)(0d)

  @Transient lazy val rand = new Random()

  override def run(sourceContext: SourceFunction.SourceContext[CarEvent]): Unit = {
    while (isRunning) {
      Thread.sleep(100)

      for (carId <- 0 until 2) {
        if (rand.nextBoolean()) speeds(carId) = Math.min(100, speeds(carId) + 5)
        else speeds(carId) = Math.max(0, speeds(carId) - 5)

        distances(carId) += speeds(carId) / 3.6d
        val record = CarEvent(carId, speeds(carId), distances(carId), System.currentTimeMillis())
        sourceContext.collect(record)
      }
    }
  }

  override def cancel(): Unit = isRunning = false
}
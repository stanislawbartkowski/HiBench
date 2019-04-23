/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.hibench.common.streaming.metrics

import java.util.concurrent.Callable
import java.util.Properties
import java.time.Duration
import java.util

import scala.collection.JavaConversions._
import org.apache.kafka.common.TopicPartition
import com.codahale.metrics.Histogram
import org.apache.kafka.clients.consumer.KafkaConsumer


class FetchJob(prop: Properties, topic: String, partition: Int, min : Int, histogram: Histogram) extends Callable[FetchJobResult] {

  private var lastDur : Duration = Duration.ZERO

  private def timeNotElapsed(start: Long): Boolean = {
    val duration: Duration = Duration.ofMillis(util.Calendar.getInstance().getTimeInMillis - start)
    if (((duration.toMillis - lastDur.toMillis) / 1000) > 30) {
      println("Partiton: " + partition + " " + duration.toMillis/1000 + " sec elapsed ...")
      lastDur = duration
    }
    val mins = duration.toMinutes
    duration.toMinutes < min
  }

  override def call(): FetchJobResult = {
    val result = new FetchJobResult()
    println("Topic " + topic + " partition:" + partition + " Reading data for " + min + " minutes")
    val kafkaConsumer = new KafkaConsumer[String, String](prop)
    val pinfo: TopicPartition = new TopicPartition(topic, partition)
    // java list of topics
    val listP: java.util.List[TopicPartition] = util.Arrays.asList(pinfo)
    kafkaConsumer.assign(listP)
    //    kafkaConsumer.subscribe(Seq(topic))
    // go to the end of sream, read only current messages, ignore historical
    kafkaConsumer.seekToEnd(listP)
    val duration: Duration = Duration.ofSeconds(10)
    val start: Long = util.Calendar.getInstance().getTimeInMillis
    while (timeNotElapsed(start)) {
      val results = kafkaConsumer.poll(duration)
      for (record <- results) {
        val times = record.value().split(":")
        val startTime = times(0).toLong
        val endTime = times(1).toLong
//        println(startTime + " "  + endTime + " " + Math.max(0, endTime - startTime))
        // correct negative value which might be caused by difference of system time
        histogram.update(Math.max(0, endTime - startTime))
        result.update(startTime, endTime)
      }
    }
    println(s"Collected ${result.count} results for partition: ${partition}")
    result
  }
}

class FetchJobResult(var minTime: Long, var maxTime: Long, var count: Long) {

  def this() = this(Long.MaxValue, Long.MinValue, 0)

  def update(startTime: Long, endTime: Long): Unit = {
    count += 1

    if (startTime < minTime) {
      minTime = startTime
    }

    if (endTime > maxTime) {
      maxTime = endTime
    }
  }
}

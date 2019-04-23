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

import java.io.{File, FileWriter}
import java.util.{Date, Properties}
import java.util.concurrent.{Executors, Future, TimeUnit}

import org.apache.kafka.clients.consumer.KafkaConsumer
import com.codahale.metrics.{Histogram, UniformReservoir}

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._


class KafkaCollector(bootstrap: String, kerberos: Boolean, metricsTopic: String,
                     outputDir: String, sampleNumber: Int, groupid: String, minutes: Int) extends LatencyCollector {

  private val histogram = new Histogram(new UniformReservoir(sampleNumber))

  def start(): Unit = {
    val fetchResults = ArrayBuffer.empty[Future[FetchJobResult]]
    val prop: Properties = MetricsUtil.produceProp(bootstrap, kerberos, groupid)

    val partitions = getPartitions(prop, metricsTopic)
    val threadPool = Executors.newFixedThreadPool(partitions.size)

    println("Starting MetricsReader for kafka topic: " + metricsTopic)

    partitions.foreach(partition => {
      val job = new FetchJob(prop, metricsTopic, partition, minutes, histogram)
      val fetchFeature = threadPool.submit(job)
      fetchResults += fetchFeature
    })

    threadPool.shutdown()
    threadPool.awaitTermination(30, TimeUnit.MINUTES)

    val finalResults = fetchResults.map(_.get()).reduce((a, b) => {
      val minTime = Math.min(a.minTime, b.minTime)
      val maxTime = Math.max(a.maxTime, b.maxTime)
      val count = a.count + b.count
      new FetchJobResult(minTime, maxTime, count)
    })

    report(finalResults.minTime, finalResults.maxTime, finalResults.count)
  }

  private def getPartitions(prop: Properties, topic: String): Seq[Int] = {
    val consumer = new KafkaConsumer[String, String](prop)
    try
      consumer.partitionsFor(topic).toSeq.map(_.partition())
    finally if (consumer != null) consumer.close()
  }


  private def report(minTime: Long, maxTime: Long, count: Long): Unit = {
    val outputFile = new File(outputDir, metricsTopic + ".csv")
    println(s"written out metrics to ${outputFile.getCanonicalPath}")
    val header = "time,count,throughput(msgs/s),max_latency(ms),mean_latency(ms),min_latency(ms)," +
      "stddev_latency(ms),p50_latency(ms),p75_latency(ms),p95_latency(ms),p98_latency(ms)," +
      "p99_latency(ms),p999_latency(ms)\n"
    val fileExists = outputFile.exists()
    if (!fileExists) {
      val parent = outputFile.getParentFile
      if (!parent.exists()) {
        parent.mkdirs()
      }
      outputFile.createNewFile()
    }
    val outputFileWriter = new FileWriter(outputFile, true)
    if (!fileExists) {
      outputFileWriter.append(header)
    }
    val time = new Date(System.currentTimeMillis()).toString
    val count = histogram.getCount
    val snapshot = histogram.getSnapshot
    val throughput = count * 1000 / (maxTime - minTime)
    outputFileWriter.append(s"$time,$count,$throughput," +
      s"${formatDouble(snapshot.getMax)}," +
      s"${formatDouble(snapshot.getMean)}," +
      s"${formatDouble(snapshot.getMin)}," +
      s"${formatDouble(snapshot.getStdDev)}," +
      s"${formatDouble(snapshot.getMedian)}," +
      s"${formatDouble(snapshot.get75thPercentile())}," +
      s"${formatDouble(snapshot.get95thPercentile())}," +
      s"${formatDouble(snapshot.get98thPercentile())}," +
      s"${formatDouble(snapshot.get99thPercentile())}," +
      s"${formatDouble(snapshot.get999thPercentile())}\n")
    outputFileWriter.close()
  }

  private def formatDouble(d: Double): String = {
    import java.text.DecimalFormat
    import java.text.DecimalFormatSymbols
    val decimalSymbols = DecimalFormatSymbols.getInstance
    decimalSymbols.setDecimalSeparator('.')
    new DecimalFormat("0.00", decimalSymbols).format(d)
  }

}



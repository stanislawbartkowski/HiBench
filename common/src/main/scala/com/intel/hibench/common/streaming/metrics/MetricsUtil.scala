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

import java.util.Properties

import scala.collection.JavaConversions._
import com.intel.hibench.common.streaming.Platform
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.{LongDeserializer, LongSerializer, StringDeserializer, StringSerializer}
import org.apache.kafka.clients.admin.{AdminClient, ListTopicsResult, NewTopic}


object MetricsUtil {

  val TOPIC_CONF_FILE_NAME = "metrics_topic.conf"

  def getTopic(platform: Platform, sourceTopic: String, producerNum: Int,
               recordPerInterval: Long, intervalSpan: Int): String = {
    val topic = s"${platform}_${sourceTopic}_${producerNum}_${recordPerInterval}" +
      s"_${intervalSpan}_${System.currentTimeMillis()}"
    println(s"metrics is being written to kafka topic $topic")
    topic
  }

  private def setoftopics(adminClient: AdminClient): Set[String] = {
    val l: ListTopicsResult = adminClient.listTopics()
    l.names().get().toSet
  }

  def produceProp(bootstrap: String,kerberos : Boolean,groupId : String = null): Properties = {

    val prop: Properties = new Properties()
    prop.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap)
    prop.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
    prop.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
    prop.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    prop.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    if (groupId != null)
      prop.put("group.id",groupId)

    if (kerberos) {
      prop.put("security.protocol","SASL_PLAINTEXT")
      prop.put("sasl.kerberos.service.name","kafka")
    }
    prop
  }


  def createTopic(bootstrap: String, kerberos : Boolean, topic: String, partitions: Int): Unit = {
    val prop = produceProp(bootstrap,kerberos)
    val adminClient = AdminClient.create(prop)
    println("Creating topic " + topic)
    val replicationFactor: Short = 1
    val newTopic = new NewTopic(topic, partitions, replicationFactor)
    adminClient.createTopics(List(newTopic))
    while (!(setoftopics(adminClient) contains topic)) {
      println("Wait unless topic appears")
      Thread.sleep(100)
    }


      //    val zkClient = new ZkClient(zkConnect, 6000, 6000, ZKStringSerializer)
//    try {
//      AdminUtils.createTopic(zkClient, topic, partitions, 1)
//      while (!AdminUtils.topicExists(zkClient, topic)) {
//        Thread.sleep(100)
//      }
//    } catch {
//      case e: Exception =>
//        throw e
//    } finally {
//      zkClient.close()
//    }

  }
}

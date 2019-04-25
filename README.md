# HiBench Suite [![Build Status](https://travis-ci.org/intel-hadoop/HiBench.svg?branch=master)](https://travis-ci.org/intel-hadoop/HiBench)

# Goals

That is my fork of HiBench (https://github.com/Intel-bigdata/HiBench) suite. In my environment, HDP 3.1/2.6.4 enabled for Kerberos, I found adjusting the existing benchmark frustrating. It is described here: (https://github.com/stanislawbartkowski/MyHiBench). <br>
So I decided to develop my own copy to make running the benchmark smoothly. The main obstacles I hope to resolve:
* HDP 3.1
* Kafka 10.0
* Kerberos secured cluster

# Installation
## Prerequisities
* Download *maven* if not installed yet. (https://maven.apache.org/download.cgi).<br>
* Unpack in *opt* directory and make in */usr/local/bin* a link to *mvn* executable. 
  * mvn -> /opt/apache-maven-3.6.0/bin/mvn
* Verify
>mvn
```
Apache Maven 3.6.0 (97c98ec64a1fdfee7767ce5ffb20918da4f719f3; 2018-10-24T20:41:47+02:00)
Maven home: /opt/apache-maven-3.6.0
Java version: 1.8.0_201, vendor: Oracle Corporation, runtime: /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.201.b09-2.el7_6.x86_64/jre
Default locale: pl_PL, platform encoding: UTF-8
OS name: "linux", version: "3.10.0-957.1.3.el7.x86_64", arch: "amd64", family: "unix"
```

## Download HiBench and build it
https://github.com/intel-hadoop/HiBench/blob/master/docs/build-hibench.md
<br>
The default is HDP 3.1<br>
> git clone https://github.com/intel-hadoop/HiBench.git<br>
> cd HiBench<br>
> mvn clean package<br>
## Hadoop user
For user running the benchmark:
* create */user/{user}* directory
* if Ranger is enabled, give the user privileges : *submitjob* and *admin-queue*
* give the user read/write access to Hive *default* database
## Configure *Hive*
Ambari console->Hive->Advanced->Custom hive-site.xml<br>
Add property (pay attention to | as field delimiter)

| Property | Values |
| -- | -- |
| hive.security.authorization.sqlstd.confwhitelist.append | hive.input.format\|hive.stats.autogather\|mapreduce.job.reduces\|mapreduce.job.maps 
## Configure
### conf/hibench.conf
> cd conf<br>
> cp hibench.conf.template hibench.conf<br>

As a minimum, modify the following parameters

| Parameter | Example value |
| -- | -- |
| hibench.masters.hostnames | hurds1.fyre.ibm.com,a1.fyre.ibm.com,aa1.fyre.ibm.com
| hibench.slaves.hostnames | hurds2.fyre.ibm.com,hurds3.fyre.ibm.com,hurds4.fyre.ibm.com,hurds5.fyre.ibm.com,hurds5.fyre.ibm.com
| hibench.streambench.kafka.home | /usr/hdp/current/kafka-broker |
| hibench.streambench.zkHost | a1.fyre.ibm.com:2181,aa1.fyre.ibm.com:2181,hurds1.fyre.ibm.com:2181
| hibench.streambench.kafka.brokerList | a1.fyre.ibm.com:6667
### conf/hadoop.conf
> cd conf<br>
> cp hadoop.conf.template hadoop.conf<br>

| Parameter | Example value |
| --- | --- |
| hibench.hadoop.home | /usr/hdp/current/hadoop-client
| hibench.hdfs.master |  hdfs://a1.fyre.ibm.com:8020/tmp/hibench
| hibench.hadoop.release | hdp
### conf/spark.conf
https://github.com/intel-hadoop/HiBench/blob/master/docs/run-sparkbench.md

>cd conf<br>
>cp spark.conf.template spark.conf
<br>

| Parameter | Example value |
| -- | -- |
| hibench.spark.home | /usr/hdp/current/spark2-client
| hibench.spark.master | yarn

### Test
Test Hadoop<br>
> bin/workloads/micro/wordcount/prepare/prepare.sh<br>
> bin/workloads/micro/wordcount/spark/run.sh<br>

Test Spark<br>
> bin/workloads/ml/als/prepare/prepare.sh<br>
> bin/workloads/ml/als/spark/run.sh<br>
## Kafka streaming
### Configurtion
https://github.com/intel-hadoop/HiBench/blob/master/docs/run-streamingbench.md

> vi conf/hibench.conf<br>

| Parameter | Example value|
| --- | --- |
| hibench.streambench.kafka.home | /usr/hdp/current/kafka-broker |
| hibench.streambench.zkHost | a1.fyre.ibm.com:2181,aa1.fyre.ibm.com:2181,hurds1.fyre.ibm.com:2181
| hibench.streambench.kafka.brokerList | a1.fyre.ibm.com:6667

<br>
In *conf/spark.conf* replace *hibench.streambench.spark.checkpointPath* parameter with HDFS value accessible for the user running the test.<br>

> vi conf/spark.conf

| Parameter | Value |
| -- | -- |
| hibench.streambench.spark.checkpointPath | /tmp |

### vi bin/workloads/streaming/identity/prepare/dataGen.sh
Modify the script to get access to HDFS file system. Enhance *-cp* parameter with Hadoop client jars.
```
#JVM_OPTS="-Xmx1024M -server -XX:+UseCompressedOops -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:+CMSScavengeBeforeRemark -XX:+DisableExplicitGC -Djava.awt.headless=true -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dkafka.logs.dir=bin/../logs -cp ${DATATOOLS}"

JVM_OPTS="-Xmx1024M -server -XX:+UseCompressedOops -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:+CMSScavengeBeforeRemark -XX:+DisableExplicitGC -Djava.awt.headless=true -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dkafka.logs.dir=bin/../logs -cp $HADOOP_HOME/*:$HADOOP_HOME/client/*:${DATATOOLS}"
```
### Prepare data
> bin/workloads/streaming/identity/prepare/genSeedDataset.sh<br>
### Start producing stream of data
> bin/workloads/streaming/identity/prepare/dataGen.sh<br>
```
============ StreamBench Data Generator ============
 Interval Span       : 50 ms
 Record Per Interval : 5
 Record Length       : 200 bytes
 Producer Number     : 1
 Total Records        : -1 [Infinity]
 Total Rounds         : -1 [Infinity]
 Kafka Topic          : identity
====================================================
Estimated Speed :
    100 records/second
    0.02 Mb/second
====================================================
log4j:WARN No appenders could be found for logger (org.apache.kafka.clients.producer.ProducerConfig).
log4j:WARN Please initialize the log4j system properly.
log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.
opening all parts in path: hdfs://mdp1.sb.com:8020/tmp/bench/HiBench/Streaming/Seed/uservisits, from offset: 0
pool-1-thread-1 - starting generate data ...
```
## Test using Kafka command line tools
(command example)
>  /usr/hdp/current/kafka-broker/bin/kafka-console-consumer.sh --bootstrap-server  mdp1.sb.com:6667 --topic identity<br>
>  /usr/hdp/current/kafka-broker/bin/kafka-console-consumer.sh --security-protocol PLAINTEXTSASL --bootstrap-server a1.fyre.ibm.com:6667,aa1.fyre.ibm.com:6667,hurds1.fyre.ibm.com:6667 --topic identity<br>

(stream of lines)<br>
```
39568	163.48.19.64,cnwtiocjooynqvcjwrplpnexcomgpybcrqriswbfcpnazkaqkwckuqitjfznhhgbbzzwphzmqfafqdkjxlafycm,1975-03-19,0.1348409,Mozilla/5.0 (Windows; U; Windows NT 5.2) AppleWebKit/525.13
39568	169.95.134.151,cnwtiocjooynqvcjwrplpnexcomgpybcrqriswbfcpnazkaqkwckuqitjfznhhgbbzzwphzmqfafqdkjxlafycm,2007-07-02,0.23636532,Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0),ISL,I
39568	142.5.139.121,cnwtiocjooynqvcjwrplpnexcomgpybcrqriswbfcpnazkaqkwckuqitjfznhhgbbzzwphzmqfafqdkjxlafycm,1979-09-02,0.99028385,Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0),BEL,BE
39568	57.174.175.181,cnwtiocjooynqvcjwrplpnexcomgpybcrqriswbfcpnazkaqkwckuqitjfznhhgbbzzwphzmqfafqdkjxlafycm,1994-12-23,0.4787203,Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0),AUS,AU
39576	178.227.245.86,rbugnwgtufibpnzapyujlpkycickwftijhmfiaffhmxhvlgevubmxnmeoyrn,2003-05-25,0.44906622,Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1),PHL,PHL-EN,Sakha,6
39576	207.206.180.144,rbugnwgtufibpnzapyujlpkycickwftijhmfiaffhmxhvlgevubmxnmeoyrn,2011-07-07,0.989489,Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1),POL,POL-PL,sulphured,6
39576	119.181.133.206,rbugnwgtufibpnzapyujlpkycickwftijhmfiaffhmxhvlgevubmxnmeoyrn,1980-03-01,0.5762792,Pzheuxmiqiggwls/3.3,ARE,ARE-AR,inevitable,10
39576	90.190.160.33,rbugnwgtufibpnzapyujlpkycickwftijhmfiaffhmxhvlgevubmxnmeoyrn,1974-02-07,0.3832044,Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1),ISL,ISL-IS,firemen,2
39576	233.99.172.182,rbugnwgtufibpnzapyujlpkycickwftijhmfiaffhmxhvlgevubmxnmeoyrn,1989-10-19,0.7538247,Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1),ROU,ROU-RO,paramedic,1
39576	217.33.120.134,rbugnwgtufibpnzapyujlpkycickwftijhmfiaffhmxhvlgevubmxnmeoyrn,1972-03-04,0.37720335,Wqzfuwmaqsleg/5.1,SVK,SVK-SK,knifes,8
39576	83.184.206.126,rbugnwgtufibpnzapyujlpkycickwftijhmfiaffhmxhvlgevubmxnmeoyrn,1997-03-19,0.76973563,Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0),PRY,PRY-ES,Agustin,3


```
### In parallel, run the Spark streaming client
> bin/workloads/streaming/identity/spark/run.sh<br>
```
19/03/19 15:33:17 INFO Client:
         client token: N/A
         diagnostics: N/A
         ApplicationMaster host: 192.168.122.74
         ApplicationMaster RPC port: 0
         queue: default
         start time: 1553005993879
         final status: UNDEFINED
         tracking URL: http://mdp1.sb.com:8088/proxy/application_1552994510056_0051/
         user: bench
19/03/19 15:33:17 INFO YarnClientSchedulerBackend: Application application_1552994510056_0051 has started running.
19/03/19 15:33:17 INFO Utils: Successfully started service 'org.apache.spark.network.netty.NettyBlockTransferService' on port 43370.
19/03/19 15:33:17 INFO NettyBlockTransferService: Server created on mdp1.sb.com:43370
19/03/19 15:33:17 INFO BlockManager: Using org.apache.spark.storage.RandomBlockReplicationPolicy for block replication policy
19/03/19 15:33:17 INFO BlockManagerMaster: Registering BlockManager BlockManagerId(driver, mdp1.sb.com, 43370, None)
19/03/19 15:33:17 INFO BlockManagerMasterEndpoint: Registering block manager mdp1.sb.com:43370 with 673.5 MB RAM, BlockManagerId(driver, mdp1.sb.com, 43370, None)
19/03/19 15:33:17 INFO BlockManagerMaster: Registered BlockManager BlockManagerId(driver, mdp1.sb.com, 43370, None)
19/03/19 15:33:17 INFO BlockManager: Initialized BlockManager: BlockManagerId(driver, mdp1.sb.com, 43370, None)
19/03/19 15:33:17 INFO JettyUtils: Adding filter org.apache.hadoop.yarn.server.webproxy.amfilter.AmIpFilter to /metrics/json.
19/03/19 15:33:17 INFO ContextHandler: Started o.s.j.s.ServletContextHandler@1291aab5{/metrics/json,null,AVAILABLE,@Spark}
19/03/19 15:33:20 INFO YarnSchedulerBackend$YarnDriverEndpoint: Registered executor NettyRpcEndpointRef(spark-client://Executor) (192.168.122.82:36746) with ID 1
19/03/19 15:33:20 INFO BlockManagerMasterEndpoint: Registering block manager mdp4.sb.com:42709 with 673.5 MB RAM, BlockManagerId(1, mdp4.sb.com, 42709, None)
19/03/19 15:33:20 INFO YarnSchedulerBackend$YarnDriverEndpoint: Registered executor NettyRpcEndpointRef(spark-client://Executor) (192.168.122.105:41418) with ID 2
19/03/19 15:33:20 INFO YarnClientSchedulerBackend: SchedulerBackend is ready for scheduling beginning after reached minRegisteredResourcesRatio: 0.8
19/03/19 15:33:20 INFO BlockManagerMasterEndpoint: Registering block manager mdp2.sb.com:37615 with 673.5 MB RAM, BlockManagerId(2, mdp2.sb.com, 37615, None)
```
In tiny environment, the Spark client can fail.
```
19/03/27 22:02:06 INFO BlockManagerMasterEndpoint: Registering block manager mdp2.sb.com:33446 with 620.1 MB RAM, BlockManagerId(2, mdp2.sb.com, 33446, None)
java.util.concurrent.RejectedExecutionException: Task org.apache.spark.streaming.CheckpointWriter$CheckpointWriteHandler@2f33e0c8 rejected from java.util.concurrent.Threa...
        at java.util.concurrent.ThreadPoolExecutor$AbortPolicy.rejectedExecution(ThreadPoolExecutor.java:2063)
        at java.util.concurrent.ThreadPoolExecutor.reject(ThreadPoolExecutor.java:830)
        at java.util.concurrent.ThreadPoolExecutor.execute(ThreadPoolExecutor.java:1379)
        at org.apache.spark.streaming.CheckpointWriter.write(Checkpoint.scala:290)
        at org.apache.spark.streaming.scheduler.JobGenerator.doCheckpoint(JobGenerator.scala:297)
        at org.apache.spark.streaming.scheduler.JobGenerator.org$apache$spark$streaming$scheduler$JobGenerator$$processEvent(JobGenerator.scala:186)
        at org.apache.spark.streaming.scheduler.JobGenerator$$anon$1.onReceive(JobGenerator.scala:89)
        at org.apache.spark.streaming.scheduler.JobGenerator$$anon$1.onReceive(JobGenerator.scala:88)
        at org.apache.spark.util.EventLoop$$anon$1.run(EventLoop.scala:48)

```
The solution is to modify *hibench.streambench.spark.batchInterval* parameter in *conf/spark.conf*
```
#======================================================
# Spark Streaming
#======================================================
# Spark streaming Batchnterval in millisecond (default 100)
#hibench.streambench.spark.batchInterval          100
hibench.streambench.spark.batchInterval          200

```

### Get metrics from time to time
>  bin/workloads/streaming/identity/common/metrics_reader.sh
```
start MetricsReader bench
SPARK_identity_1_5_50_1552999658022
SPARK_identity_1_5_50_1553000124410
SPARK_identity_1_5_50_1553000236815
SPARK_identity_1_5_50_1553001022513
SPARK_identity_1_5_50_1553001112518
SPARK_identity_1_5_50_1553001343170
SPARK_identity_1_5_50_1553003441519
SPARK_identity_1_5_50_1553005025887
SPARK_identity_1_5_50_1553005572004
SPARK_identity_1_5_50_1553005923312
SPARK_identity_1_5_50_1553005989549
__consumer_offsets
ambari_kafka_service_check
identity
Please input the topic:
```
Enter the latest topic and get the result as csv file.
====================
====================
## The bigdata micro benchmark suite ##


* Current version: 7.0
* Homepage: https://github.com/intel-hadoop/HiBench
* Contents:
  1. Overview
  2. Getting Started
  3. Workloads
  4. Supported Releases

---
### OVERVIEW ###

HiBench is a big data benchmark suite that helps evaluate different big data frameworks in terms of speed, throughput and system resource utilizations. It contains a set of Hadoop, Spark and streaming workloads, including Sort, WordCount, TeraSort, Sleep, SQL, PageRank, Nutch indexing, Bayes, Kmeans, NWeight and enhanced DFSIO, etc. It also contains several streaming workloads for Spark Streaming, Flink, Storm and Gearpump.

### Getting Started ###
 * [Build HiBench](docs/build-hibench.md)
 * [Run HadoopBench](docs/run-hadoopbench.md)
 * [Run SparkBench](docs/run-sparkbench.md)
 * [Run StreamingBench](docs/run-streamingbench.md) (Spark streaming, Flink, Storm, Gearpump)

### Workloads ###

There are totally 19 workloads in HiBench. The workloads are divided into 6 categories which are micro, ml(machine learning), sql, graph, websearch and streaming.

  **Micro Benchmarks:**

1. Sort (sort)

    This workload sorts its *text* input data, which is generated using RandomTextWriter.

2. WordCount (wordcount)

    This workload counts the occurrence of each word in the input data, which are generated using RandomTextWriter. It is representative of another typical class of real world MapReduce jobs - extracting a small amount of interesting data from large data set.

3. TeraSort (terasort)

    TeraSort is a standard benchmark created by Jim Gray. Its input data is generated by Hadoop TeraGen example program.

4. Sleep (sleep)

    This workload sleep an amount of seconds in each task to test framework scheduler.

5. enhanced DFSIO (dfsioe)

    Enhanced DFSIO tests the HDFS throughput of the Hadoop cluster by generating a large number of tasks performing writes and reads simultaneously. It measures the average I/O rate of each map task, the average throughput of each map task, and the aggregated throughput of HDFS cluster. Note: this benchmark doesn't have Spark corresponding implementation.


**Machine Learning:**

1. Bayesian Classification (Bayes)

    Naive Bayes is a simple multiclass classification algorithm with the assumption of independence between every pair of features. This workload is implemented in spark.mllib and uses the automatically generated documents whose words follow the zipfian distribution. The dict used for text generation is also from the default linux file /usr/share/dict/linux.words.ords.

2. K-means clustering (Kmeans)

    This workload tests the K-means (a well-known clustering algorithm for knowledge discovery and data mining) clustering in spark.mllib. The input data set is generated by GenKMeansDataset based on Uniform Distribution and Guassian Distribution.

3. Logistic Regression (LR)

    Logistic Regression (LR) is a popular method to predict a categorical response. This workload is implemented in spark.mllib with LBFGS optimizer and the input data set is generated by LogisticRegressionDataGenerator based on random balance decision tree. It contains three different kinds of data types, including categorical data, continuous data, and binary data.

4. Alternating Least Squares (ALS)

    The alternating least squares (ALS) algorithm is a well-known algorithm for collaborative filtering. This workload is implemented in spark.mllib and the input data set is generated by RatingDataGenerator for a product recommendation system.

5. Gradient Boosting Trees (GBT)

    Gradient-boosted trees (GBT) is a popular regression method using ensembles of decision trees. This workload is implemented in spark.mllib and the input data set is generated by GradientBoostingTreeDataGenerator.

6. Linear Regression (Linear)

    Linear Regression (Linear) is a workload that implemented in spark.mllib with SGD optimizer. The input data set is generated by LinearRegressionDataGenerator.

7. Latent Dirichlet Allocation (LDA)

    Latent Dirichlet allocation (LDA) is a topic model which infers topics from a collection of text documents. This workload is implemented in spark.mllib and the input data set is generated by LDADataGenerator.

8. Principal Components Analysis (PCA)

    Principal component analysis (PCA) is a statistical method to find a rotation such that the first coordinate has the largest variance possible, and each succeeding coordinate in turn has the largest variance possible. PCA is used widely in dimensionality reduction. This workload is implemented in spark.mllib. The input data set is generated by PCADataGenerator.

9. Random Forest (RF)

    Random forests (RF) are ensembles of decision trees. Random forests are one of the most successful machine learning models for classification and regression. They combine many decision trees in order to reduce the risk of overfitting. This workload is implemented in spark.mllib and the input data set is generated by RandomForestDataGenerator.

10. Support Vector Machine (SVM)

    Support Vector Machine (SVM) is a standard method for large-scale classification tasks. This workload is implemented in spark.mllib and the input data set is generated by SVMDataGenerator.

11. Singular Value Decomposition (SVD)

    Singular value decomposition (SVD) factorizes a matrix into three matrices. This workload is implemented in spark.mllib and its input data set is generated by SVDDataGenerator.


**SQL:**

1. Scan (scan), Join(join), Aggregate(aggregation)

    These workloads are developed based on SIGMOD 09 paper "A Comparison of Approaches to Large-Scale Data Analysis" and HIVE-396. It contains Hive queries (Aggregation and Join) performing the typical OLAP queries described in the paper. Its input is also automatically generated Web data with hyperlinks following the Zipfian distribution.

**Websearch Benchmarks:**

1. PageRank (pagerank)

    This workload benchmarks PageRank algorithm implemented in Spark-MLLib/Hadoop (a search engine ranking benchmark included in pegasus 2.0) examples. The data source is generated from Web data whose hyperlinks follow the Zipfian distribution.

2. Nutch indexing (nutchindexing)

    Large-scale search indexing is one of the most significant uses of MapReduce. This workload tests the indexing sub-system in Nutch, a popular open source (Apache project) search engine. The workload uses the automatically generated Web data whose hyperlinks and words both follow the Zipfian distribution with corresponding parameters. The dict used to generate the Web page texts is the default linux dict file.

**Graph Benchmark:**

1. NWeight (nweight) 

    NWeight is an iterative graph-parallel algorithm implemented by Spark GraphX and pregel. The algorithm computes associations between two vertices that are n-hop away. 


**Streaming Benchmarks:**

1. Identity (identity)

    This workload reads input data from Kafka and then writes result to Kafka immediately, there is no complex business logic involved.

2. Repartition (repartition)

    This workload reads input data from Kafka and changes the level of parallelism by creating more or fewer partitionstests. It tests the efficiency of data shuffle in the streaming frameworks.
    
3. Stateful Wordcount (wordcount)

    This workload counts words cumulatively received from Kafka every few seconds. This tests the stateful operator performance and Checkpoint/Acker cost in the streaming frameworks.
    
4. Fixwindow (fixwindow)

    The workloads performs a window based aggregation. It tests the performance of window operation in the streaming frameworks.
  
    
### Supported Hadoop/Spark/Flink/Storm/Gearpump releases: ###

  - Hadoop: Apache Hadoop 2.x, CDH5, HDP
  - Spark: Spark 1.6.x, Spark 2.0.x, Spark 2.1.x, Spark 2.2.x
  - Flink: 1.0.3
  - Storm: 1.0.1
  - Gearpump: 0.8.1
  - Kafka: 0.8.2.2

---



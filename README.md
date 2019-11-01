# HiBench Suite [![Build Status](https://travis-ci.org/intel-hadoop/HiBench.svg?branch=master)](https://travis-ci.org/intel-hadoop/HiBench)

# Goals

That is my fork of HiBench (https://github.com/Intel-bigdata/HiBench) suite. In my environment, HDP 3.1/2.6.4 enabled for Kerberos, I found adjusting the existing benchmark frustrating. It is described here: (https://github.com/stanislawbartkowski/MyHiBench). <br>
So I decided to develop my own copy to make running the benchmark smoothly. The main obstacles I hope to resolve:
* HDP 3.1
* Kafka 2.0
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
### Run HiBench test
> bin/run_all.sh<br>

### Troubleshooting
```
java.lang.NoClassDefFoundError: junit/framework/TestCase
	at java.lang.ClassLoader.defineClass1(Native Method)
	at java.lang.ClassLoader.defineClass(ClassLoader.java:763)
	at java.security.SecureClassLoader.defineClass(SecureClassLoader.java:142)
	at java.net.URLClassLoader.defineClass(URLClassLoader.java:467)
	at java.net.URLClassLoader.access$100(URLClassLoader.java:73)
	at java.net.URLClassLoader$1.run(URLClassLoader.java:368)
	at java.net.URLClassLoader$1.run(URLClassLoader.java:362)
	at java.security.AccessController.doPrivileged(Native Method)
	at java.net.URLClassLoader.findClass(URLClassLoader.java:361)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:424)
	at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:331)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:357)
	at org.apache.hadoop.test.MapredTestDriver.<init>(MapredTestDriver.java:109)
	at org.apache.hadoop.test.MapredTestDriver.<init>(MapredTestDriver.java:61)
```
Check the script file and verify that junit jar path filename is correct. 
>  vi /data/testdata/HiBench/bin/workloads/micro/sleep/hadoop/run.sh
```
export HADOOP_CLASSPATH=hadoopbench/nutchindexing/target/dependency/junit-3.8.1.jar
run_hadoop_job $HADOOP_SLEEP_JAR sleep -m $NUM_MAPS -r $NUM_REDS -mt $MAP_SLEEP_TIME -mr $RED_SLEEP_TIME

```

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

<br>

If the test is executed on the node without Kafka Broker installed, it is necessary to download and install manually the Kafka client software and modify the *hibench.streambench.kafka.home* accordingly. For instance, assuming Kafka installed under */opt/kafka_2.12-2.3.1/* directory:<br>

| Parameter | Value |
| -- | -- |
| hibench.streambench.kafka.home | /opt/kafka_2.12-2.3.1/ |

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

# Kerberos
## Configuration
Modify *security* parameter to *kerberos*. 
> vi conf/hibench.com
```
#security
# kerberos,nonsecure
hibench.security        kerberos
# only if kerberos is set, used for spark streaming, distribute computing
hibench.benchkeytab jaas/bench_jaas.conf
hibench.benchjaas jaas/bench.keytab
```
## Users
Make sure that user running the benchmark is authorized:
* *yarn* : submit jobs in *root.default* queue
* *hive* : read/write access in *default* database
* *kafka* : create and write topics
## Prepare *jaas* file to run distribute Spark/Kafka streaming job
Next step is to prepare Kerberos *keytab* file and JAAS policy file. It is described here: https://github.com/stanislawbartkowski/SampleSparkStreaming/blob/master/README.md#kerberos-keytab<br>
 
Assume:
* jaas/bench.keytab : keytab file
* jaas/bench_jaas.conf : JAAS policy file

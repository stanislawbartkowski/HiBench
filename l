patching args=
start HadoopDfsioe-read bench
Deleted hdfs://a1.fyre.ibm.com:8020/tmp/hibench/HiBench/Dfsioe/Input/io_read
Submit MapReduce Job: /usr/hdp/current/hadoop-client/bin/hadoop --config /usr/hdp/current/hadoop-client/etc/hadoop jar /home/bench/HiBench/autogen/target/autogen-7.1-SNAPSHOT.jar org.apache.hadoop.fs.dfsioe.TestDFSIOEnh -Dmapreduce.map.java.opts="-Dtest.build.data=hdfs://a1.fyre.ibm.com:8020/tmp/hibench/HiBench/Dfsioe/Input -Xmx3276m" -Dmapreduce.reduce.java.opts="-Dtest.build.data=hdfs://a1.fyre.ibm.com:8020/tmp/hibench/HiBench/Dfsioe/Input -Xmx6553m" -read -nrFiles 16 -fileSize 1 -bufferSize 131072 -plotInteval 1000 -sampleUnit m -sampleInteval 200 -sumThreshold 0.5 -tputReportTotal -Dtest.build.data=hdfs://a1.fyre.ibm.com:8020/tmp/hibench/HiBench/Dfsioe/Input -resFile /home/bench/HiBench/report/dfsioe/hadoop/conf/../result_read.txt -tputFile /home/bench/HiBench/report/dfsioe/hadoop/conf/../throughput_read.csv
TestFDSIO.0.0.4 Enhanced Version
ERROR: Hadoop job /home/bench/HiBench/autogen/target/autogen-7.1-SNAPSHOT.jar org.apache.hadoop.fs.dfsioe.TestDFSIOEnh failed to run successfully.
Hint: You can goto /home/bench/HiBench/report/dfsioe/hadoop/conf/../bench.log to check for detailed log.
Opening log tail for you:

patching args=
start HadoopDfsioe-write bench

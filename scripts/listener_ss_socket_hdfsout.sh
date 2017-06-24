#!/bin/bash
# You should already have a data source on port 9999

export PORTNUMBER=9999
$SPARK_HOME/bin/spark-submit  --class com.createksolutions.sessionizer.SessionizeData  --master local[4]  --executor-memory 512M  target/SparkStreamingSessionization.jar socket  hdfs://192.170.152.76:9000/user/cloud/ss/results hdfs://192.170.152.76:9000/user/cloud/ss/checkpoint localhost $PORTNUMBER



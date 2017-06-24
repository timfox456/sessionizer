#!/bin/bash
export PORTNUMBER=9999
$SPARK_HOME/bin/spark-submit  --class com.createksolutions.sessionizer.SessionizeData  --master local[4]  --executor-memory 512M  target/SparkStreamingSessionization.jar socket ~/ss/results ~/ss/checkpoint localhost $PORTNUMBER



package com.createksolutions.sessionizer

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream.PairDStreamFunctions
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.dstream.DStream
import org.apache.hadoop.io.LongWritable
import java.text.SimpleDateFormat
import org.apache.hadoop.fs.Path
import org.apache.spark.streaming.dstream.FileInputDStream
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import scala.collection.immutable.HashMap
import java.util.Date

/**
  * Created by tfox on 12/26/15.
  */
object TestStreaming {


  val OUTPUT_ARG = 1
  val CHECKPOINT_DIR_ARG = 2
  val FIXED_ARGS = 3


  def main(args: Array[String]) {
    if (args.length == 0) {
      println("TestStreaming {sourceType} {outputDir} {source information}")
      println("TestStreaming file {outputDir} {hdfs checkpoint directory} {source file}")
      println("TestStreaming newFile {outputDir} {hdfs checkpoint directory} {source file}")
      println("TestStreaming localFile {outputDir} {hdfs checkpoint directory} {source file}")
      println("TestStreaming socket {outputDir} {hdfs checkpoint directory} {host} {port}")
      return;
    }

    val outputDir = args(OUTPUT_ARG)
    val checkpointDir = args(CHECKPOINT_DIR_ARG)


    //This is just creating a Spark Config object.  I don’t do much here but
    //add the app name.  There are tons of options to put into the Spark config,
    //but none are needed for this simple example.
    val sparkConf = new SparkConf().
      setAppName("TestStreaming " + args(0)).
      set("spark.cleaner.ttl", "120000")

    //These two lines will get us out SparkContext and our StreamingContext.
    //These objects have all the root functionality we need to get started.
    val sc = new SparkContext(sparkConf)
    val ssc = new StreamingContext(sc, Seconds(10))

    //This is create a reference to our root DStream.  DStreams are like RDDs but
    //with the context of being in micro batch world.  I set this to null now
    //because I later give the option of populating this data from HDFS or from
    //a socket.  There is no reason this could not also be populated by Kafka,
    //Flume, MQ system, or anything else.  I just focused on these because
    //there are the easiest to set up.
    var lines: DStream[String] = null

    //Options for data load.  Will be adding Kafka and Flume at some point
    if (args(0).equals("socket")) {
      val host = args(FIXED_ARGS);
      val port = args(FIXED_ARGS + 1);

      println("host:" + host)
      println("port:" + Integer.parseInt(port))

      //Simple example of how you set up a receiver from a Socket Stream
      lines = ssc.socketTextStream(host, port.toInt)
    } else if (args(0).equals("newFile")) {

      val directory = args(FIXED_ARGS)
      println("directory:" + directory)

      //Simple example of how you set up a receiver from a HDFS folder
      lines = ssc.fileStream[LongWritable, Text, TextInputFormat](directory, (t: Path) => true, true).map(_._2.toString)
    } else if (args(0).equals("localFile")) {

      val directory = args(FIXED_ARGS)
      println("directory:" + directory)

      //Simple example of how you set up a receiver from a HDFS folder
      lines = ssc.textFileStream(directory)
    } else {
      throw new RuntimeException("bad input type")
    }


    lines.print()
    lines.saveAsTextFiles(outputDir + "/lines", "txt")

    ssc.checkpoint(checkpointDir)

    ssc.start
    ssc.awaitTermination
    }
  }

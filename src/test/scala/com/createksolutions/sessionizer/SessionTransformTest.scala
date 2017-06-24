package com.createksolutions.sessionizer

import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.rdd.RDD
import org.apache.spark._
import org.apache.spark.streaming._

import org.scalatest._

import scala.collection.mutable

/**
  * Created by tfox on 12/26/15.
  */
class SessionTransformTest extends FlatSpec {

  private var sc: SparkContext = _
  private var ssc: StreamingContext = _

   "ParseLinesTestOrig" should "CorrectlyParse the Lines" in {

     /*
    val lines = mutable.Queue[RDD[String]]()
    val dstream = ssc.queueStream(lines)
    val testLines = Seq(
      "66.249.283.146 - - [25/Dec/2015 06:14:01 +0000] GET /support.html HTTP/1.1 200 11179 - Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
      "66.249.260.21 - - [25/Dec/2015 06:14:01 +0000] GET /bar.html HTTP/1.1 200 11179 - Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
    lines += sc.makeRDD(testLines)

    val parsedLines = SessionTransform.parseOrigLines(dstream)
*/



  }

}

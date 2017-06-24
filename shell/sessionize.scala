import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.streaming._
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.dstream.DStream

import java.util.Date

import scala.collection.immutable.HashMap
import org.apache.spark.streaming.dstream.DStream
import java.text.SimpleDateFormat

val outputDir = "~/ss/results"
val checkpointDir = "~/ss/checkpoint"
val port = "9999"
val host = "localhost"

val SESSION_TIMEOUT = (60000 * 0.5).toInt
//val dateFormatSimple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") // Used for 3rdEye project


  val TOTAL_SESSION_TIME = "TOTAL_SESSION_TIME"
  val UNDER_A_MINUTE_COUNT = "UNDER_A_MINUTE_COUNT"
  val ONE_TO_TEN_MINUTE_COUNT = "ONE_TO_TEN_MINUTE_COUNT"
  val OVER_TEN_MINUTES_COUNT = "OVER_TEN_MINUTES_COUNT"
  val NEW_SESSION_COUNTS = "NEW_SESSION_COUNTS"
  val TOTAL_SESSION_COUNTS = "TOTAL_SESSION_COUNTS"
  val EVENT_COUNTS = "EVENT_COUNTS"
  val DEAD_SESSION_COUNTS = "DEAD_SESSION_COUNTS"
  val REVISTE_COUNT = "REVISTE_COUNT"
  val TOTAL_SESSION_EVENT_COUNTS = "TOTAL_SESSION_EVENT_COUNTS"

def validateLines(lines: DStream[String]) = {
    lines.
      filter(line => (line.length() > 10) && (line.contains(","))).
      filter(line => (line.split(",").length > 3)).
      filter(line => (line.split(",")(2).length() > 10)).
      filter(line => (!line.contains("sessionization")))
  }

def parseProjLines(lines: DStream[String]) = {
val dateFormatSimple2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") // Used for 3rdEye project
    lines.
      filter(line => (line.length() > 10) && (line.contains(","))).
      filter(line => (line.split(",").length > 3)).
      filter(line => (line.split(",")(2).length() > 10)).
      filter(line => (!line.contains("sessionization"))).
      map[(String, (Long, Long, String))](eventRecord => {
      //Get the time and ip address out of the original event
      val splitData = eventRecord.split(",")
      val ipAddress = splitData(0)
      val time =  (if (!splitData(2).isEmpty) dateFormatSimple2.parse(
        splitData(2)).getTime() else 0)

      //We are return the time twice because we will use the first at the start time
      //and the second as the end time
      (ipAddress, (time, time, eventRecord))
    })
  }



  def updateStatbyOfSessions(
                              //(sessionStartTime, sessionFinishTime, countOfEvents)
                              a: Seq[(Long, Long, Long)],
                              //(sessionStartTime, sessionFinishTime, countOfEvents, isNewSession)
                              b: Option[(Long, Long, Long, Boolean)]
                            ): Option[(Long, Long, Long, Boolean)] = {

    //This function will return a Optional value.
    //If we want to delete the value we can return a optional "None".
    //This value contains four parts
    //(startTime, endTime, countOfEvents, isNewSession)
    var result: Option[(Long, Long, Long, Boolean)] = null

    // These if statements are saying if we didn’t get a new event for
    //this session’s ip address for longer then the session
    //timeout + the batch time then it is safe to remove this key value
    //from the future Stateful DStream
    if (a.size == 0) {
      if (System.currentTimeMillis() - b.get._2 < SESSION_TIMEOUT + 11000) {
        result = None
      } else {
        if (b.get._4 == false) {
          result = b
        } else {
          result = Some((b.get._1, b.get._2, b.get._3, false))
        }
      }
    }

    //Now because we used the reduce function before this function we are
    //only ever going to get at most one event in the Sequence.
    a.foreach(c => {
      if (b.isEmpty) {
        //If there was no value in the Stateful DStream then just add it
        //new, with a true for being a new session
        result = Some((c._1, c._2, c._3, true))
      } else {
        if (c._1 - b.get._2 < SESSION_TIMEOUT) {
          //If the session from the stateful DStream has not timed out
          //then extend the session
          result = Some((
            Math.min(c._1, b.get._1), //newStartTime
            Math.max(c._2, b.get._2), //newFinishTime
            b.get._3 + c._3, //newSumOfEvents
            false //This is not a new session
            ))
        } else {
          //Otherwise remove the old session with a new one
          result = Some((
            c._1, //newStartTime
            c._2, //newFinishTime
            b.get._3, //newSumOfEvents
            true //new session
            ))
        }
      }
    })
    result
  }




val sparkConf = new SparkConf().
  setAppName("SessionizeData ").
  set("spark.cleaner.ttl", "120000")

//val sc = new SparkContext(sparkConf)
val ssc = new StreamingContext(sc, Seconds(10))

val lines = ssc.socketTextStream(host, port.toInt)


    // lines is the input data:
    // User,???,Timestamp,page,code
    // Example:
    // 22215,dunno,2015-12-27 21:36:15,/cart/programming in C#,code
    // 37772,dunno,2015-12-27 21:36:15,/cart/Drama Queen,code

    val validatedLines = validateLines(lines)

    //transform to (ipAddress, (lowestStartTime, MaxFinishTime, sumOfCounter))
    // Example:
    //(90530,(1422416165000,1422416165000,90530,dunno,2015-12-27 21:36:05,/product/Science magic,code))
    //(19123,(1422416165000,1422416165000,19123,dunno,2015-12-27 21:36:05,/checkout/Martial arts,code))

    val ipKeyLines = parseProjLines(validatedLines)  // transform to (User, (StartTime, EndTime, User, StartTime, EndTime)

    val latestSessionInfo = ipKeyLines.map[(String, (Long, Long, Long))](a => {
      (a._1, (a._2._1, a._2._2, 1))
    })
    val latestSessionInfo2 = latestSessionInfo.
      reduceByKey((a, b) => {
        (Math.min(a._1, b._1), Math.max(a._2, b._2), a._3 + b._3)
      })
    val latestSessionInfo3 = latestSessionInfo2.
      updateStateByKey(updateStatbyOfSessions)

    //remove old sessions
    val onlyActiveSessions = latestSessionInfo.filter(t => System.currentTimeMillis() - t._2._2 < SESSION_TIMEOUT)
    val totals = onlyActiveSessions.mapPartitions[(Long, Long, Long, Long)](it =>
    {
      var totalSessionTime: Long = 0
      var underAMinuteCount: Long = 0
      var oneToTenMinuteCount: Long = 0
      var overTenMinutesCount: Long = 0

      it.foreach(a => {
        val time = a._2._2 - a._2._1
        totalSessionTime += time
        if (time < 60000) underAMinuteCount += 1
        else if (time < 600000) oneToTenMinuteCount += 1
        else overTenMinutesCount += 1
      })

      Iterator((totalSessionTime, underAMinuteCount, oneToTenMinuteCount, overTenMinutesCount))
    }, true).reduce((a, b) => {
      //totalSessionTime, underAMinuteCount, oneToTenMinuteCount, overTenMinutesCount
      (a._1 + b._1, a._2 + b._2, a._3 + b._3, a._4 + b._4)
    }).map[HashMap[String, Long]](t => HashMap(
      (TOTAL_SESSION_TIME, t._1),
      (UNDER_A_MINUTE_COUNT, t._2),
      (ONE_TO_TEN_MINUTE_COUNT, t._3),
      (OVER_TEN_MINUTES_COUNT, t._4)))

    val newSessionCount = onlyActiveSessions.filter(t => {
      //is the session newer then that last micro batch
      //and is the boolean saying this is a new session true
      (System.currentTimeMillis() - t._2._2 < 11000 && t._2._4)
    }).
      count.
      map[HashMap[String, Long]](t => HashMap((NEW_SESSION_COUNTS, t)))

    val totalSessionCount = onlyActiveSessions.
      count.
      map[HashMap[String, Long]](t => HashMap((TOTAL_SESSION_COUNTS, t)))

    val totalSessionEventCount = onlyActiveSessions.map(a => a._2._3).reduce((a, b) => a + b).
      count.
      map[HashMap[String, Long]](t => HashMap((TOTAL_SESSION_EVENT_COUNTS, t)))

    val totalEventsCount = ipKeyLines.count.map[HashMap[String, Long]](t => HashMap((EVENT_COUNTS, t)))

    val deadSessionsCount = latestSessionInfo.filter(t => {
      val gapTime = System.currentTimeMillis() - t._2._2
      gapTime > SESSION_TIMEOUT && gapTime < SESSION_TIMEOUT + 11000
    }).count.map[HashMap[String, Long]](t => HashMap((DEAD_SESSION_COUNTS, t)))

    val allCounts = newSessionCount.
      union(totalSessionCount).
      union(totals).
      union(totalEventsCount).
      union(deadSessionsCount).
      union(totalSessionEventCount).
      reduce((a, b) => b ++ a)


    // Uncomment this if you want to debug/dump further
    lines.saveAsTextFiles(outputDir + "/lines", "txt")
    ipKeyLines.saveAsTextFiles(outputDir + "/ipkeylines", "txt")
    latestSessionInfo.saveAsTextFiles(outputDir + "/latestSessionInfo", "txt")
    onlyActiveSessions.saveAsTextFiles(outputDir + "/onlyActiveSessions", "txt")
    totals.saveAsTextFiles(outputDir + "/totals", "txt")


    allCounts.
      saveAsTextFiles(outputDir + "/allcounts", "txt")

    //Persist to HDFS
    ipKeyLines.join(onlyActiveSessions).
      map(t => {
        //Session root start time | Event message

        val dateFormatSimple2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") // Used for 3rdEye project
        dateFormatSimple2.format(new Date(t._2._2._1)) + "\t" + t._2._1._3
      }).
      saveAsTextFiles(outputDir + "/session", "txt")


ssc.checkpoint(checkpointDir)

ssc.start
ssc.awaitTermination

//ssc.close()
//sc.close()


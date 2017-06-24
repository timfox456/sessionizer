package com.createksolutions.sessionizer

/**
  * Created by tfox on 12/27/15.
  *
  * To run, use 'generator_run.sh' script in 'scripts'
  */


import org.slf4j.{Logger, LoggerFactory}

import scala.util.Random
import scala.collection.mutable.ListBuffer
import com.github.nscala_time.time.Imports._

object StreamGenerator {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  // batch parameters
  // every X seconds you will get a new batch of log data
  val batchFrequencySeconds = 20
  // your batch size will be X session parameters
  val batchSize = 50
  var batchCollector = new ListBuffer[String]()
  // time parameters
  // time from the beginning of simulation, in seconds
  var runTime = 0
  val random = new Random

  val totalNumberOfUsers = 100000
  val intervalLength = 1000000
  val activeUserPercentage = 0.01
  // probability of changing to another page
  val userChangePage = 0.1
  // probability of going away from the site
  val userBounce = 0.025
  // by the way userChangePage/userBounce gives you the average number of pages visited in a session

  val webPages = List("/cart/Assessment book", "/cart/Banking world", "/cart/Christmas books", "/cart/Drama Queen", "/cart/Dream science", "/cart/driving_jarvis_ham",
    "/cart/Environment psychology", "/cart/Invisible world", "/cart/Making history", "/cart/Managing meetings", "/cart/Mona Lisa", "/cart/Political suicide",
    "/cart/programming in C#", "/cart/Science plus", "/cart/science_c72", "/cart/the_world_a35", "/cart/World atlas", "/checkout/academic_art",
    "/checkout/Applied psychology", "/checkout/Bill books", "/checkout/Business management", "/checkout/california_science_a40", "/checkout/Christmas books",
    "/checkout/Giants bread", "/checkout/Martial arts", "/checkout/my world", "/checkout/popular music", "/checkout/Science anytime",
    "/checkout/Science directions", "/checkout/the_world_a35", "/checkout/Understanding music", "/checkout/Yoruba poetry",
    "/product/Agricultural science", "/product/Body politic", "/product/Book lover", "/product/book_bus_songs", "/product/Environmental science",
    "/product/Fantasy art", "/product/Fiction", "/product/Health psychology", "/product/Managing yourself", "/product/Murder book", "/product/Musicals",
    "/product/paper_music", "/product/psychology_c57", "/product/Science anytime", "/product/Science explorer", "/product/Science magic",
    "/product/Silent music", "/product/spotlight_books", "/product/Venus Drive"
  )

  // prepare a map of all users on the site
  var activeUserMap = new scala.collection.mutable.HashMap[Int, String]

  //val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
  val VERSION = "V0.1"

  def main(args: Array[String]): Unit = {
    logger.info(VERSION + " Stream generation of customer web log data started")
    println(VERSION + " Stream generation of customer web log data started")
    println("To stop, do CTRL-C")
    // start with the given number of active users
    val numberUsers = (totalNumberOfUsers * activeUserPercentage).toInt
    for (u <- 1 to numberUsers) {
      // give the random user a random page to start with
      val user = random.nextInt(totalNumberOfUsers)
      val page = random.nextInt(webPages.size)
      activeUserMap += (user -> webPages(page))
    }

    while (true) {
      runTime = runTime + 1
      simulateNextSecond
      // if we are at end of a batch, send emit all records
      if (runTime % batchFrequencySeconds == 0) {
        emitRecords
      }

    }

    def simulateNextSecond(): Unit = {
      // let the users go to the next page and record the new positions
      activeUserMap.foreach(x => {
        val user = x._1
        val page = x._2

        var activeUser = -1
        if (random.nextDouble() < userBounce) {
          activeUserMap -= (user)
          // if we remove a user, add a random one to maintain the average load
          activeUser = random.nextInt(totalNumberOfUsers)
          activeUserMap += (activeUser -> webPages(random.nextInt(webPages.size)))
        } else {
          if (random.nextDouble() < userChangePage) {
            activeUserMap += (user -> webPages(random.nextInt(webPages.size)))
            activeUser = user
          }
        }
        if (activeUser >= 0) {
          batchCollector += (user.toString + "," + "dunno" + "," + DateTime.now.toString(dateFormat) + "," + page + "," + "code")

        }
      })

      Thread.sleep(1000)
    }

    def emitRecords(): Unit = {
      //logger.info("Emitting {} records at {} seconds", batchCollector.length, runTime)
      // TODO output to a file or socket
      batchCollector.foreach(str => {
        // TODO append to a file to which you want to write
        println(str)
        
        // TODO write to socket instead of reading from. But this will only work if someone is listening
//        val socket = new Socket(InetAddress.getByName("localhost"), 9999)
//        lazy val in = new BufferedSource(socket.getInputStream()).getLines()
//        val out = new PrintStream(socket.getOutputStream())
//
//        try {
//          val ia = InetAddress.getByName("localhost")
//          val socket = new Socket(ia, 9999)
//          val out = new ObjectOutputStream(
//            new DataOutputStream(socket.getOutputStream()))
//          out.writeObject(str)
//          out.flush()
//          out.close()
//          socket.close()
//        }
//        catch {
//          case e: IOException =>
//            e.printStackTrace()
//        }
      })
      batchCollector = new ListBuffer[String]()
    }
  }
}

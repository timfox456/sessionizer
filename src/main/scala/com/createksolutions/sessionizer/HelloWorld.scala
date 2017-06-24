package com.createksolutions.sessionizer

/**
  * Created by mark on 12/27/15.
  */
import org.slf4j.{Logger, LoggerFactory}

object HelloWorld {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {
    logger.info("Logging started")
    if (args.length == 0) {
      println("I need one argument, and that should be your name")
      System.exit(0)
    }
    println("Shalom, " + args(0))
    logger.debug("Also logging")
  }
}

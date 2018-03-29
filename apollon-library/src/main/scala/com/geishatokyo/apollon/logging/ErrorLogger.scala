package com.geishatokyo.apollon.logging

import java.util.Date

import com.geishatokyo.apollon.util.json.JsonHelper
import org.slf4j.LoggerFactory

/**
 * Created by yamaguchi on 15/08/07.
 */
object ErrorLogger{

  val logger = LoggerFactory.getLogger(getClass)
}


sealed trait ErrorLevel {
  def writer : (String, Throwable) => Unit
  def log(message : String, e: Throwable) : Unit = writer(message, e)
}

object ErrorLevel {
  case class Regular() extends ErrorLevel {
    override val toString = "Regular"
    def writer = ErrorLogger.logger.info
  }

  case class Validation() extends ErrorLevel {
    override val toString = "Validation"
    def writer = ErrorLogger.logger.warn
  }

  case class Server() extends ErrorLevel {
    override val toString = "Server"
    def writer = ErrorLogger.logger.error
  }
}
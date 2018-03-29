package com.geishatokyo.apollon.global

import com.typesafe.config.Config
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._

/**
 * Created by takeshita on 2014/06/16.
 */
object Accessors {

  val logger = LoggerFactory.getLogger(getClass)

  var unsafe : Boolean = false
  var permitClients : List[String] = Nil

  def init(conf : Config) = {
    permitClients = conf.getStringList("permitClients").toList
    logger.info(s"permitClients : $permitClients")
  }

  def shutdown() = {
  }

}

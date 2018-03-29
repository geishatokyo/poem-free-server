package com.geishatokyo.apollon.logging

import java.util.Date

import com.geishatokyo.apollon.util.json.JsonHelper
import org.slf4j.LoggerFactory

import scala.collection.immutable.ListMap

/**
 * Created by takezoux2 on 15/01/23.
 */

trait ActionLogger {
  def log(userId : Long,action : Action,params : (String,Any)*) : Unit
  def log(action : Action,params : (String,Any)*) : Unit
}
object ActionLogger {

  val logger = LoggerFactory.getLogger(getClass)

  private val slf4jLogger = new Slf4jActionLogger

  def getLogger(implicit logger: ActionLogger = slf4jLogger): ActionLogger = {
    logger
  }

  def format(action : Action, userId : String = "undefined", params : Seq[(String,Any)] ): String = {
    val now = new Date().getTime
    val log = ListMap(
      "time" -> now,
      "userId" -> userId,
      "action" -> action.action,
      "phase" -> action.phase,
      "params" -> params.toMap
    )
    JsonHelper.toCompactString(log)
  }
}
class Slf4jActionLogger extends ActionLogger {

  def log(userId : Long,action : Action,params : (String,Any)*) : Unit = {
    log(ActionLogger.format(action = action, userId = userId.toString, params = params))
  }

  def log(action : Action,params : (String,Any)*) : Unit = {
    log(ActionLogger.format(action = action, params = params))
  }

  private def log(log : String) = ActionLogger.logger.info(log)

}

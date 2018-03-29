package com.geishatokyo.apollon.error

import java.util.Date

import com.geishatokyo.apollon.logging.{Action, ActionLogger, ErrorLevel}
import com.geishatokyo.apollon.util.json.JsonHelper
import org.json4s.JsonDSL._

/**
 * Created by yamaguchi on 2015/08/10.
 */
trait APIException extends Throwable{
  def code : Int
  def message : String
  def params : Seq[(String,Any)]
  def level : ErrorLevel

  def writeLog(userId: String = "undefined") = {
    // とりあえず、スタックトレースは保留
    ActionLogger.getLogger.log(userId.toLong, Action.ErrorTrace, (("code"-> code) +: params) : _*)
    level.log(makeLog(userId), this)
  }

  def makeLog(userId : String): String =
    formatLog("Error." + level + "." + code, ("userId" -> userId) +: ("message" -> message) +: params)

  def makeLog: String = formatLog("Error." + level + "." + code, ("message" -> message) +: params)

  def formatLog(tag: String,params : Seq[(String,Any)] ): String =
    JsonHelper.toCompactString(List(tag, new Date().getTime, params.toMap))(JsonHelper.normalFormats)

  def toJValue = ("resultCode" -> code) ~ ("message" -> message)

  @inline
  def throws = {
    throw this
  }
}

case class ServerException(code : Int, message : String, params : (String,Any)*) extends RuntimeException(message) with APIException {
  // サーバー内部のエラー
  val level = ErrorLevel.Server()
}

case class ValidationException(code : Int, message : String, params : (String,Any)*) extends Exception(message) with APIException {
  // クライアントレベルのエラー
  val level = ErrorLevel.Validation()
}

case class RegularException(code : Int, message : String, params : (String,Any)*) extends Exception(message) with APIException {
  // 正常に発生するエラー
  val level = ErrorLevel.Regular()
}
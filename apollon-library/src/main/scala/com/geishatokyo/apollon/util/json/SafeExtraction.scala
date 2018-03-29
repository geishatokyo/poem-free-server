package com.geishatokyo.apollon.util.json

import java.util.Date

import com.geishatokyo.apollon.error.ErrorCode
import org.json4s.Formats
import org.json4s.JsonAST._

/**
 * Created by takeshita on 2015/01/19.
 */
object SafeExtraction {

  def int(j : JValue,key : String,default : => Option[Int] = None) = {
    (j \ key) match{
      case JInt(v) => v.toInt
      case JDouble(v) => v.toInt
      case JString(v) => v.toInt
      case JNothing => default.getOrElse{ ErrorCode.WrongJsonFormat("key" -> key).throws}
      case _ => {
        ErrorCode.WrongJsonFormat("key" -> key, "type" -> "Int").throws
      }
    }
  }

  def long(j : JValue,key : String,default : => Option[Long] = None) = {
    (j \ key) match{
      case JInt(v) => v.toLong
      case JDouble(v) => v.toLong
      case JString(v) => v.toLong
      case JNothing => default.getOrElse{ ErrorCode.WrongJsonFormat("key" -> key).throws}
      case _ => {
        ErrorCode.WrongJsonFormat("key" -> key, "type" -> "Long").throws
      }
    }
  }

  def string(j : JValue,key : String,default : => Option[String] = None) : String = {
    (j \ key) match{
      case JInt(v) => v.toString
      case JDouble(v) => v.toString
      case JString(v) => v
      case JNothing => default.getOrElse{ ErrorCode.WrongJsonFormat("key" -> key).throws}
      case _ => {
        ErrorCode.WrongJsonFormat("key" -> key, "type" -> "String").throws
      }
    }
  }
  def boolean(j : JValue,key : String,default : => Option[Boolean] = None) = {
    (j \ key) match{
      case JInt(v) => v != 0
      case JDouble(v) => v != 0
      case JString(v) => v.toBoolean
      case JBool(v) => v
      case JNothing => default.getOrElse{ ErrorCode.WrongJsonFormat("key" -> key).throws}
      case _ => {
        ErrorCode.WrongJsonFormat("key" -> key, "type" -> "Boolean").throws
      }
    }
  }
  def double(j : JValue,key : String,default : => Option[Double] = None) = {
    (j \ key) match{
      case JInt(v) => v.toDouble
      case JDouble(v) => v.toDouble
      case JString(v) => v.toDouble
      case JNothing => default.getOrElse{ ErrorCode.WrongJsonFormat("key" -> key).throws}
      case _ => {
        ErrorCode.WrongJsonFormat("key" -> key, "type" -> "Double").throws
      }
    }
  }
  def date(j : JValue,key : String,default : => Option[Date] = None) = {
    (j \ key) match{
      case JInt(v) => new Date(v.toLong * 1000) // unix time
      case JDouble(v) => new Date((v.toDouble * 1000).toLong) // Unitx time
      case JString(v) => new Date(v.toLong) // Unitx time
      case JNothing => default.getOrElse{ ErrorCode.WrongJsonFormat("key" -> key).throws}
      case _ => {
        ErrorCode.WrongJsonFormat("key" -> key, "type" -> "Date").throws
      }
    }
  }



  def obj[T](j : JValue,key : String)(implicit m : Manifest[T], formats: Formats = JsonHelper.infoTypeFormats) = {
    JsonHelper.extract[T](j \ key)
  }

}

package com.geishatokyo.apollon.util.json

import java.util.Date

import com.geishatokyo.apollon.json.InfoTypeHints
import org.json4s
import org.json4s.JsonAST.{JDouble, JInt, JString, JValue}
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.reflect.TypeInfo

/**
 * Created by takeshita on 2014/06/16.
 */
object JsonHelper {
  val normalFormats = DefaultFormats + UnixTimeDateSerializer

  val infoTypeFormats = new InfoTypeFormats + UnixTimeDateSerializer

  val defaultFormats = infoTypeFormats

  class CommandTypeFormats extends DefaultFormats{
    override val typeHintFieldName: String = "class"
    override val typeHints: TypeHints = new ClassNameTypeHints()
    override val dateFormat: DateFormat = MyDateFormat
  }

  class InfoTypeFormats extends DefaultFormats{
    override val typeHintFieldName: String = "infoType"
    override val typeHints: TypeHints = new InfoTypeHints()
    override val dateFormat: DateFormat = MyDateFormat
  }

  val DateTypeInfo = TypeInfo(classOf[Date],None)

  object UnixTimeDateSerializer extends Serializer[Date]{
    override def deserialize(implicit format: Formats): PartialFunction[(json4s.TypeInfo, json4s.JValue), Date] = {
      case (DateTypeInfo,JInt(unixTimeSec)) => new Date((unixTimeSec * 1000).toLong)
      case (DateTypeInfo,JString(unixTimeSec)) => new Date(unixTimeSec.toLong * 1000)
      case (DateTypeInfo,JDouble(unixTimeSec)) => new Date((unixTimeSec * 1000).toLong)
    }

    override def serialize(implicit format: Formats): PartialFunction[Any, json4s.JValue] = {
      case d : Date => JInt(d.getTime / 1000)
    }
  }

  object MyDateFormat extends DateFormat{
    def parse(s: String): Option[Date] = {
      try{
        Some(new Date(s.toLong * 1000))
      }catch{
        case e : NumberFormatException => {

          None
        }
        case e : Throwable => {
          None
        }
      }
    }

    def format(d: Date): String = {
      d.getTime / 1000 toString
    }
  }

  object RigidDateFormat extends DateFormat{
    def parse(s: String): Option[Date] = {
      try{
        Some(new Date(s.toLong))
      }catch{
        case e : NumberFormatException => {
          None
        }
        case e : Throwable => {
          None
        }
      }
    }

    def format(d: Date): String = {
      d.getTime toString
    }
  }


  def extract[T]( json : JValue)(implicit m : Manifest[T], formats: Formats = defaultFormats) = {
    Extraction.extract[T](json)
  }

  def toJValue( obj : Any)(implicit formats: Formats = defaultFormats) = {
    Extraction.decompose(obj)
  }

  def toString(json : JValue) = pretty(render(json))

  def toString(obj : Any) : String = toString(toJValue(obj))

  def fromString( str : String) = {
    parse(str)
  }

  def toCompactString(obj : Any)(implicit formats: Formats = defaultFormats) : String = {
    compact(render(toJValue(obj)))
  }

  def extractFromString[T](str : String)(implicit m : Manifest[T], formats: Formats = normalFormats) = {
    extract[T](fromString(str))
  }

  def extractFromStringWithInfoType[T](str : String)(implicit m : Manifest[T], formats: Formats = defaultFormats) = {
    extract[T](fromString(str))
  }

}

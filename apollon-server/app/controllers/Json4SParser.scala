package controllers

import play.api.mvc.{RequestHeader, Results, BodyParsers, BodyParser}
import org.json4s.JsonAST.JValue
import play.api.Logger
import play.api.libs.iteratee.{Done,Traversable, Iteratee}
import org.json4s.native.JsonMethods._
import org.json4s.ParserUtil.ParseException
import play.api.libs.iteratee.Input.{El, Empty}
import play.api.libs.concurrent.Execution.Implicits._
import com.geishatokyo.apollon.error.ErrorCode

/**
 * 
 * User: takeshita
 * DateTime: 13/09/10 12:05
 */

class Json4SParser extends BodyParser[(JValue,String)]{

  val logger = Logger.logger

  def apply( rh : RequestHeader) = {
    Traversable.takeUpTo[Array[Byte]](BodyParsers.parse.DefaultMaxTextLength)
      .apply({
        Iteratee.consume[Array[Byte]]().map(b => {
          try {
            val v = parse(new String(b, "utf-8"))
            Right(v -> "") // 簡易サーバで無認証なのでbodyのhashによる改ざんチェックは省略
          } catch {
            case e: ParseException => {
              if (logger.isDebugEnabled) {
                logger.error("Wrong json format for request %s:%s.JSON = %s".format(rh.method, rh.uri, new String(b, "utf-8")))
              } else {
                // Do not print json in production
                logger.error("Wrong json format for request %s:%s".format(rh.method, rh.uri))
              }
              Left(Results.BadRequest(pretty(render(ErrorCode.WrongJsonFormat().toJValue))) -> b)
            }
          }
        })
      })
      .flatMap(Iteratee.eofOrElse( Results.EntityTooLarge))
      .flatMap {
        case Left(b) => Done(Left(b), Empty)
        case Right(it) => it.flatMap {
          case Left((r, in)) => Done(Left(r), El(in))
          case Right(json) => Done(Right(json), Empty)
        }
      }
  }
}

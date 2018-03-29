package controllers

import com.geishatokyo.apollon.error._
import com.geishatokyo.apollon.global.Accessors
import com.geishatokyo.apollon.util.json.JsonHelper
import org.json4s.Extraction
import org.json4s.JsonAST.{JNothing, JValue}
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods
import org.json4s.native.JsonMethods._
import org.slf4j.LoggerFactory
import play.api.mvc._

import scala.concurrent.Future

/**
  *
  * User: yamaguchi
  * DateTime: 18/03/05
  */
trait BaseController extends Controller {

  val logger = LoggerFactory.getLogger(this.getClass)

  implicit val jsonFormats = JsonHelper.infoTypeFormats

  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  def acceptableVersions = Accessors.permitClients

  def success[T](entry: T) = {
    val res = ("resultCode" -> ErrorCodeDef.Success) ~ ("entry" -> Extraction.decompose(entry))
    logger.debug("success value : \n" + pretty(JsonMethods.render(res)))
    res
  }

  def clientVersion(implicit req: Request[_]) = {
    req.headers.get(CustomHeader.X_CLIENT_VERSION).getOrElse("")
  }

  def checkClientVersion(implicit req: Request[_]) = {
    logger.debug(s"clientVersion : ${clientVersion}")
    if(acceptableVersions.nonEmpty)
      acceptableVersions
        .find(acceptable => acceptable == clientVersion)
        .getOrElse(ErrorCode.UnacceptableVersion(
          "clientVersion" -> clientVersion,
          "acceptableVersions" -> acceptableVersions).throws)
  }

  def SyncReq(func: Request[JValue] => JValue) = Action(new Json4SParser()) { implicit req =>
    try_!(req){
      checkClientVersion
      logger.debug(s"receive: ")
      if(req.body._1 == JNothing) logger.debug("nothing")
      else
        logger.debug(pretty(JsonMethods.render(req.body._1)))
      jsonResponse(Ok, func(req.map(_._1)))
    }
  }

  def AsyncReq(func: (Request[JValue] => Future[JValue])) = Action.async(new Json4SParser()) { implicit req =>
    futureTry(req) {
      checkClientVersion
      func(req.map(_._1))
        .transform(jsonResponse(Ok, _), e => e)
    }
  }

  def try_!(req: Request[(JValue, String)])(func: => Result): Result = {
    implicit val request = req
    try {
      func
    } catch {errorHandle}
  }

  def futureTry(req: Request[(JValue, String)])(func: => Future[Result]): Future[Result] = {
    implicit val request = req
    try {
      func.recoverWith {case e => Future(errorHandle(e))}
    } catch {case e : Throwable => Future(errorHandle(e))}
  }

  def jsonResponse(code: Status, res: JValue): Result = {
    logger.debug(s"response: \n${pretty(JsonMethods.render(res))}")
    code(compact(JsonMethods.render(res))).as("application/json;charset=UTF-8")
  }

  def errorHandle : PartialFunction[Throwable, Result] = {
    case e: RegularException =>
      e.printStackTrace()
      jsonResponse(Ok, e.toJValue)
    case e: ValidationException =>
      e.printStackTrace()
      jsonResponse(BadRequest, e.toJValue)
    case e: ServerException =>
      e.printStackTrace()
      jsonResponse(InternalServerError, e.toJValue)
    case e: Throwable =>
      e.printStackTrace()
      val err = ErrorCode.UnknownError(e)
      jsonResponse(InternalServerError, err.toJValue)
  }
}

object CustomHeader {
  val X_PLATFORM_KIND = "X-PLATFORM-KIND"
  val X_CLIENT_VERSION  = "X-CLIENT-VERSION"
}

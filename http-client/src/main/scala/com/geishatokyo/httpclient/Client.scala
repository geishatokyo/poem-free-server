package com.geishatokyo.httpclient

import com.geishatokyo.apollon.model.InfoBase
import com.geishatokyo.apollon.util.json.JsonHelper

import dispatch._
import org.json4s._
import org.json4s.native.JsonMethods._
import scala.concurrent.ExecutionContext.Implicits.global


object Client{
  var baseUrl = "http://localhost:9000"
  var clientVersion = "3.0.2"

  implicit val formats = JsonHelper.infoTypeFormats

  def ping = {
    val service = url("/status/ping")
    val req = Http(service OK as.String)
    println(req())
  }

  def post(path : String, envelope : InfoBase = null) = {
    val body = if(envelope == null) "" else JsonHelper.toCompactString(envelope)
    val v = post_native(path, body)
    val bd = parse(v)
    println(JsonHelper.toString(bd))
    val entry = bd \ "entry"
    JsonHelper.extract[InfoBase](entry)
  }

  def post_native(path : String, body : String = "") = {
    val service = url(baseUrl + path).POST
    val ser = service
      .setContentType("application/json", "UTF-8")
      .addHeader("X-CLIENT-VERSION", clientVersion)
      .setBody(body.getBytes("utf-8"))
    val req = Http(ser OK as.String)
    req()
  }
}

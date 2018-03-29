package controllers

import javax.inject.Inject

import modules.Global
import play.api.mvc._

class Application @Inject() (global : Global) extends Controller {

  def index = Action( req => {
    Ok(views.html.index())
  })

  def ping = Action{
    Ok("pong")
  }
}
package com.geishatokyo.apollon.logging

/**
  * Created by fujiwarakazuya on 2015/11/20.
  */
object Action {

  //##replace definition
  val BattlepostCreateRoom = Action("Battle.postCreateRoom")
  val BattlepostJoinRoom = Action("Battle.postJoinRoom")
  val BattlepostAcceptRoom = Action("Battle.postAcceptRoom")
  val BattlepostLeaveRoom = Action("Battle.postLeaveRoom")
  val BattlepostReceiveApiEvents = Action("Battle.postReceiveApiEvents")
  val BattlepostSendOperation = Action("Battle.postSendOperation")
  //##end

  val ErrorTrace = Action("Error.Trace").log
  def apply(action : String) : ActionOnly = {
    new ActionOnly(action)
  }

  class ActionOnly(action : String) {
    def start : Action = {
      phase("start")
    }

    def end : Action = {
      phase("end")
    }

    def error : Action = {
      phase("error")
    }

    def log : Action = {
      phase("log")
    }

    def phase(phase : String) = {
      Action(action, phase)
    }
  }
}
case class Action(action : String, phase : String)
package com.geishatokyo.apollon.battle

trait ApiEvent{
  val eventId : Int
  val player : BattlePlayer.Value
}

object ApiEvent {
  case class LeaveApiEvent(eventId : Int, player : BattlePlayer.Value, reason : String) extends ApiEvent
  case class OperationApiEvent(eventId : Int, player : BattlePlayer.Value, operation : String) extends ApiEvent
}

package com.geishatokyo.apollon.battle

trait BattleMessage
trait BattleMessageMember extends BattleMessage {val selfToken : String}

object BattleMessage {
  // KeepALive
  case class SetReceiver(selfToken : String, sinceId : Long) extends BattleMessageMember
  case class ApiEventsResponse(apiEvents: List[ApiEvent]) extends BattleMessage
  // Join
  case class JoinRoom(playerData : String) extends BattleMessage
  case class JoinRoomResponse(playerToken : Option[String], seed : Int) extends BattleMessage
  // ACCEPT
  case class AcceptRoom(selfToken : String) extends BattleMessageMember
  case class AcceptRoomResponse(enemyPlayerData : String, isOwner : Boolean) extends BattleMessage
  // Leave
  case class LeaveRoom(selfToken : String, reason : String) extends BattleMessageMember
  // Operation
  case class Operation(selfToken : String, operation : String) extends BattleMessageMember
  case class OperationResponse(apiEvent: ApiEvent) extends BattleMessage

  // Close どの状態で終了状態に遷移するかわからない
  case class RoomCloseResponse() extends BattleMessage
  // 不正アクセスかも？メンバー以外によるメンバーとしてのアクセス
  case class NotMemberResponse() extends BattleMessage
}

package controllers.api

import javax.inject.Inject

import com.geishatokyo.apollon.action._
import com.geishatokyo.apollon.util.json.SafeExtraction
import controllers.BaseController
import modules.Global

/**
 * Auto generated
 */
class BattleController @Inject() (global : Global) extends BaseController {

  implicit def ec = scala.concurrent.ExecutionContext.Implicits.global

  //@insert[methods]
  def postCreateRoom = SyncReq(implicit req => {
    val r = BattleAction.postCreateRoom()
    success(r)
  })
  def postJoinRoom = AsyncReq(implicit req => {
    val roomCode = SafeExtraction.string(req.body,"roomCode")
    val playerData = SafeExtraction.string(req.body,"playerData")
    val r = BattleAction.postJoinRoom(roomCode, playerData)
    r.transform(res => success(res), e => e)
  })
  def postAcceptRoom = AsyncReq(implicit req => {
    val battleId = SafeExtraction.long(req.body,"battleId")
    val playerToken = SafeExtraction.string(req.body,"playerToken")
    val r = BattleAction.postAcceptRoom(battleId, playerToken)
    r.transform(res => success(res), e => e)
  })
  def postLeaveRoom = SyncReq(implicit req => {
    val battleId = SafeExtraction.long(req.body,"battleId")
    val playerToken = SafeExtraction.string(req.body,"playerToken")
    val reason = SafeExtraction.string(req.body,"reason")
    val r = BattleAction.postLeaveRoom(battleId, playerToken, reason)
    success(r)
  })
  def postReceiveApiEvents = AsyncReq(implicit req => {
    val battleId = SafeExtraction.long(req.body,"battleId")
    val playerToken = SafeExtraction.string(req.body,"playerToken")
    val sinceApiEventId = SafeExtraction.long(req.body,"sinceApiEventId")
    val r = BattleAction.postReceiveApiEvents(battleId, playerToken, sinceApiEventId)
    r.transform(res => success(res), e => e)
  })
  def postSendOperation = AsyncReq(implicit req => {
    val battleId = SafeExtraction.long(req.body,"battleId")
    val playerToken = SafeExtraction.string(req.body,"playerToken")
    val operation = SafeExtraction.string(req.body,"operation")
    val r = BattleAction.postSendOperation(battleId, playerToken, operation)
    r.transform(res => success(res), e => e)
  })

  //@end

}
    
package com.geishatokyo.apollon.battle

import java.security.SecureRandom

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.collection.mutable
import com.geishatokyo.apollon.error.ErrorCode
import com.geishatokyo.apollon.global.BattleConst
import com.geishatokyo.apollon.battle.BattleMessage._

import scala.concurrent.ExecutionContext

/**
  * Created by yamaguchi on 2018/3/2.
  */
object BattleManager {

  val logger = LoggerFactory.getLogger(this.getClass)

  val rooms : mutable.Map[Long, ActorRef] = mutable.HashMap.empty[Long, ActorRef]
  val random = new scala.util.Random(SecureRandom.getInstance("NativePRNGNonBlocking"))

  var actorSystem : ActorSystem = null
  implicit var executionContext : ExecutionContext = null

  def init(actorSystem: ActorSystem, executionContext: ExecutionContext) = {
    this.actorSystem = actorSystem
    this.executionContext = executionContext
  }

  def createRoom() = synchronized {
    val battleId = genBattleId(rooms.keys, 10)
    val actor : ActorRef = actorSystem.actorOf(Props(classOf[RoomActor]))

    rooms.put(battleId, actor)
    logger.info(s"CreateRoom { battleId : $battleId, actorRef : $actor }")
    // フリー用の簡略化でbattleIdをroomCodeとして使用する
    "%05d" format battleId
  }

  def join(battleId : Long, visitorDeck : String) = {
    val timeout = Timeout((BattleConst.JoinWaitSecond + BattleConst.MarginSecond) second)
    call(battleId, JoinRoom(visitorDeck))(timeout).transform({
      case JoinRoomResponse(Some(playerToken), seed) => (playerToken, seed)
      case _ : JoinRoomResponse => ErrorCode.OverJoinRoom().throws
      case _ : RoomCloseResponse => ErrorCode.JoinTimeOut().throws
    }, e => e)
  }

  def accept(battleId : Long, playerToken : String) = {
    val timeout = Timeout((BattleConst.AcceptWaitSecond + BattleConst.MarginSecond) second)
    call(battleId, AcceptRoom(playerToken))(timeout).transform({
      case response : AcceptRoomResponse => (response.enemyPlayerData, response.isOwner)
      case _ : RoomCloseResponse => ErrorCode.OpponentParingAcceptTimeOut().throws
      case _ : NotMemberResponse => ErrorCode.NotPairingBattle().throws
    }, e => e)
  }

  def operation(battleId : Long, playerToken : String, operation : String) = {
    val timeout = Timeout(BattleConst.MarginSecond second)
    call(battleId, Operation(playerToken, operation))(timeout).transform({
      case response : OperationResponse => response.apiEvent
      case _ : NotMemberResponse => ErrorCode.NotPairingBattle().throws
    }, e => e)
  }

  def setReceiver(battleId : Long, playerToken : String, sinceId : Long) = {
    val timeout = Timeout((BattleConst.EventWaitSecond + BattleConst.MarginSecond) second)
    call(battleId, SetReceiver(playerToken, sinceId))(timeout).transform({
      case response: ApiEventsResponse => response.apiEvents.sortBy(_.eventId)
      case _: RoomCloseResponse => Nil
      case _ : NotMemberResponse => ErrorCode.NotPairingBattle().throws
    }, e => e)
  }

  def leave(battleId : Long, playerToken : String, reason : String) : Unit = {
    send(battleId, LeaveRoom(playerToken, reason))
  }


  def terminateRoomByActorRef(actorRef : ActorRef) = synchronized {
    rooms.find(_._2 == actorRef).foreach(r => rooms.remove(r._1))
  }

  private def send(battleId : Long, message : BattleMessage) = {
    getRoomActor(battleId) ! message
  }
  private def call(battleId : Long, message : BattleMessage)(implicit timeout : Timeout)  = {
    getRoomActor(battleId) ? message
  }
  private def getRoomActor(battleId : Long) = synchronized {
    rooms.get(battleId).getOrElse(ErrorCode.BattleRoomNotFoundByRoomCode().throws)
  }

  @scala.annotation.tailrec
  private def genBattleId(battleIds : Iterable[Long], remaining : Int) : Long = {
    val battleId = random.nextInt(100000)
    battleIds.find(_ == battleId) match {
      case None => battleId
      case _ if remaining == 0 => ErrorCode.CantCreateRoom().throws
      case _ => genBattleId(battleIds, remaining -1)
    }
  }

  def genPlayerToken(prefix : String) : String = {
    val token = "%07d" format random.nextInt(10000000)
    prefix + token
  }

  def genBattleSeed() : Int = random.nextInt(10000000)
}

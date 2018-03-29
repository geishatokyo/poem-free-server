package com.geishatokyo.apollon.battle

import com.geishatokyo.apollon.battle.BattleMessage._
import com.geishatokyo.apollon.battle.ApiEvent._
import akka.actor.{Actor, ActorRef, FSM}
import com.geishatokyo.apollon.global.BattleConst
import org.joda.time.DateTime

import scala.concurrent.duration._

/**
 * Created by yamaguchi on 2018/3/2.
 */
case class RoomStatus(apiEvents : List[ApiEvent], players : List[PlayerStatus], stateLimitTime : DateTime, stopStandby : Boolean){
  def updateStateLimitTime(seconds : Int) = copy(stateLimitTime = DateTime.now.plusSeconds(seconds))
}
case class PlayerStatus(playerType: BattlePlayer.Value, token : String, playerData : String, accepted : Boolean, receiver: Option[ReceiverStatus], lastAlive : DateTime)
case class ReceiverStatus(receiver : ActorRef, sinceId : Long)
class RoomActor() extends Actor with FSM[Symbol, RoomStatus]{
  implicit val executionContext = context.system.dispatcher

  startWith('JOIN, RoomStatus(Nil, Nil, DateTime.now.plusSeconds(BattleConst.JoinWaitSecond), false))

  // 参加待ち状態
  when('JOIN, stateTimeout = BattleConst.PollIntervalMilliSecond millisecond){ handle {
    case Event(join : JoinRoom, status @ RoomStatus(Nil, Nil, _, _)) =>
      val playerToken = BattleManager.genPlayerToken("owner-")
      val playerStatus = PlayerStatus(BattlePlayer.Owner, playerToken, join.playerData, false, Some(ReceiverStatus(sender(), 0)), DateTime.now)
        stay using status.copy(players = List(playerStatus))
    case Event(join : JoinRoom, status @ RoomStatus(Nil, List(owner), _, _)) =>
      val playerToken = BattleManager.genPlayerToken("visitor-")
      val playerStatus = PlayerStatus(BattlePlayer.Visitor, playerToken, join.playerData, false, None, DateTime.now)
      val newPlayers = List(owner.copy(receiver = None, lastAlive = DateTime.now), playerStatus)
      val seed = BattleManager.genBattleSeed()
      List(owner.receiver.map(_.receiver).get -> owner.token, sender() -> playerToken)
        .foreach({case (ref, token) => ref ! JoinRoomResponse(Some(token), seed)})
      BattleManager.logger.debug(s"[${this.self}] goto accept")
      goto('ACCEPT) using status.copy(players = newPlayers).updateStateLimitTime(BattleConst.AcceptWaitSecond)
    /*
     * このステートではクライアントから明示的にcloseさせないので注意
     * フリー公開なので無駄に作りまくるアタックについてはアプリ修正の時間都合上断念
     * 全部イベント式にしてあったらkeep alive で比較的早く止めれただろうに。。。
     */
  }}

  when('ACCEPT, stateTimeout = BattleConst.PollIntervalMilliSecond millisecond){ handle {
    case Event(accept : AcceptRoom, status @ RoomStatus(Nil, players, _, _)) =>
      val newPlayers = updatePlayerStatus(accept.selfToken, players) {_.copy(accepted = true, receiver = Some(ReceiverStatus(sender(), 0)))}
      println(s"accept : $accept, status : $status")
      if(newPlayers.forall(_.accepted)) {
        newPlayers.foreach(ps => {
          val receiver = ps.receiver.get.receiver
          val enemyPlayerData = newPlayers.find(_.playerType != ps.playerType).get.playerData
          val isOwner = ps.playerType == BattlePlayer.Owner
          receiver ! AcceptRoomResponse(enemyPlayerData, isOwner)
        })
        val newPlayersForPlay = newPlayers.map(_.copy(receiver = None, lastAlive = DateTime.now))
        BattleManager.logger.debug(s"[${this.self}] goto play")
        goto('PLAY) using status.copy(players = newPlayersForPlay).updateStateLimitTime(BattleConst.RoomTimeLimitSecond)
      }
      else {
        stay using status.copy(players = newPlayers)
      }
    case Event(_ : LeaveRoom, _ : RoomStatus) =>
      stop
  }}

  // 操作情報をapiEventsに保存してレシーバに配信する
  when('PLAY, stateTimeout = BattleConst.PollIntervalMilliSecond millisecond){ handle {
    case Event(operation : Operation, status) =>
      val newStatus = addApiEvent(status){
        OperationApiEvent(
          _,
          getMember(operation.selfToken, status).get,
          operation.operation
        )
      }
      sender() ! OperationResponse(newStatus.apiEvents.head)
      stay using newStatus

    case Event(setReceiver : SetReceiver, status @ RoomStatus(_, players, _, _)) =>
      val diffApiEvents = filterEvents(getMember(setReceiver.selfToken, status).get, setReceiver.sinceId,status.apiEvents)
      val newPlayers = {
        if (diffApiEvents.isEmpty) {
          updatePlayerStatus(setReceiver.selfToken, players){
            _.copy(receiver = Some(ReceiverStatus(sender(), setReceiver.sinceId)))
          }
        }
        else {
          sender() ! ApiEventsResponse(diffApiEvents)
          updatePlayerStatus(setReceiver.selfToken, players){_.copy(receiver = None)}
        }
      }
      stay using status.copy(players = updatePlayerStatus(setReceiver.selfToken, newPlayers){_.copy(lastAlive = DateTime.now())})

    // 新規ApiEventがレシーバの待機時間内に発生しなかった
    case Event(StateTimeout, status @ RoomStatus(_, players, _, _))
      if players.find(p => p.receiver.isDefined && p.lastAlive.isBefore(DateTime.now.minusSeconds(20))).isDefined =>
      println(s"timeout ${players.map(p => s"pT : ${p.playerType}, ${p.lastAlive}").mkString("|")}")
      val newPlayers = players.map{
        case player if player.receiver.isDefined && player.lastAlive.isBefore(DateTime.now.minusSeconds(20)) =>
          player.receiver.get.receiver ! ApiEventsResponse(Nil)
          player.copy(receiver = None)
        case player => player
      }
      stay using status.copy(players = newPlayers)

    // Keep Alive していないので切断と判断して部屋を閉じる時刻を早める
    case Event(StateTimeout, status @ RoomStatus(_, players, _, stopStandby))
      if !stopStandby && players.find(_.lastAlive.isBefore(DateTime.now.minusSeconds(30))).isDefined =>
      println(s"stopStandby timeout ${players.map(p => s"pT : ${p.playerType}, ${p.lastAlive}").mkString("|")}")
      val newStatus = players.filter(_.lastAlive.isBefore(DateTime.now.minusSeconds(30)))
        .foldLeft(status)((s, ps) =>{
          addApiEvent(s){LeaveApiEvent(_, ps.playerType, "ERROR")}
        })
      stay using newStatus.copy(stateLimitTime = DateTime.now.plusSeconds(60), stopStandby = true)

    // 退室(Resultなど)
    case Event(leave : LeaveRoom, status : RoomStatus) =>
      val playerType = getMember(leave.selfToken, status).get
      val newStatus = addApiEvent(status){LeaveApiEvent(_, playerType, leave.reason)}
      stay using newStatus.copy(stateLimitTime = DateTime.now.plusSeconds(60))
  }}

  onTermination {
    case StopEvent(FSM.Normal, _, status) =>
      commonTermination(status)
    case StopEvent(FSM.Shutdown, state, status) =>
      BattleManager.logger.error(s"RoomActor Shutdown: {state :$state}")
      commonTermination(status)
    case StopEvent(FSM.Failure(cause), state, status) =>
      BattleManager.logger.error(s"RoomActor Failure : {state :$state, cause : $cause}")
      commonTermination(status)
  }

  // 起動
  initialize()

  /* 処理関数群 */
  def commonTermination(status : RoomStatus) = {
    BattleManager.logger.debug(s"[${this.self}] room close")
    status.players.foreach(_.receiver.foreach(_.receiver ! RoomCloseResponse()))
    BattleManager.terminateRoomByActorRef(self)
  }

  // エラーハンドリングと共通イベントハンドラ呼び出しの共通化
  def handle(func : StateFunction): StateFunction = {
    case event_ @Event(event, stateData) =>
      try {
        if (event.isInstanceOf[BattleMessageMember] &&
          getMember(event.asInstanceOf[BattleMessageMember].selfToken, stateData).isEmpty
        ) {
          sender() ! NotMemberResponse()
          stay
        }
        else {
          (func orElse defaultHandler) (event_)
        }
      }
      catch {
        case e: Throwable => stop(FSM.Failure(e))
      }
  }

  // 状態共通でのハンドリング
  def defaultHandler: StateFunction = {
    // 埋まっている状態でもコード公開してたら入ろうとすることもある
    case Event(_ : JoinRoom, _) =>
      // 既に埋まってる。1人目の方は先に個別処理でmatchしているはず。
      sender() ! JoinRoomResponse(None, -1)
      stay

    // 状態継続期限
    case Event(StateTimeout, RoomStatus(_, _, stopTime, _))
      if stopTime.isBeforeNow =>
      stop
    case Event(StateTimeout, RoomStatus(_, players, _, _)) =>
      stay

    // 想定外のeventのハンドリング
    case unknown @ Event(_, _) =>
      stop(FSM.Failure(unknown)) // 不明なmessageを受け取った
  }

  def getMember(token : String, status : RoomStatus) : Option[BattlePlayer.Value] =
    status.players.find(token == _.token).map(_.playerType)

  def addApiEvent(status: RoomStatus)(func : (Int) => ApiEvent) = {
    val apiEvent = func(status.apiEvents.length + 1)
    val newApiEvents = apiEvent :: status.apiEvents
    // Receiverに転送
    val newPlayers = status.players.map(responseApiEvents(_, newApiEvents))
    // ApiEventの処理が済んだroomStatusを作る
    status.copy(apiEvents = newApiEvents, players = newPlayers)
  }

  def filterEvents(self : BattlePlayer.Value, sinceId : Long, apiEvents: List[ApiEvent]) =
    apiEvents.filter(_.eventId > sinceId).filterNot(_.player == self)

  def responseApiEvents(playerStatus: PlayerStatus, apiEvents : List[ApiEvent]) = {
    playerStatus.receiver.foreach(receiver => {
      val sinceId = receiver.sinceId
      val diffApiEvents = filterEvents(playerStatus.playerType, sinceId, apiEvents)
      receiver.receiver ! ApiEventsResponse(diffApiEvents)
    })
    playerStatus.copy(receiver = None)
  }

  def updatePlayerStatus(targetToken : String, players : List[PlayerStatus])(func : (PlayerStatus) => PlayerStatus) = {
    players.map {
      case player if player.token == targetToken =>
        func(player)
      case player => player
    }
  }
}

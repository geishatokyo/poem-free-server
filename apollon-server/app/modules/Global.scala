package modules

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import com.geishatokyo.apollon.battle.BattleManager
import com.geishatokyo.apollon.global.Accessors
import play.api.Play.current
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

/**
  * Created by yamaguchi on 2018/03/19.
  */
@Singleton
class Global @Inject()(lifecycle: ApplicationLifecycle) {

  println("startup")
  val conf = current.configuration.underlying
  val actorSystem: ActorSystem = play.api.libs.concurrent.Akka.system(current)
  BattleManager.init(actorSystem, play.api.libs.concurrent.Execution.Implicits.defaultContext)

  Accessors.init(conf)

  lifecycle.addStopHook(() => {
    Future.successful({
      Accessors.shutdown()
      println("shutdown")
    })
  })

}

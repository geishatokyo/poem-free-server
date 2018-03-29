package com.geishatokyo.apollon.util.json

import org.json4s.TypeHints
import org.slf4j.LoggerFactory

/**
 * Created by takeshita on 14/03/18.
 */
class ClassNameTypeHints extends TypeHints {
  val hints: List[Class[_]] = Nil
  val logger = LoggerFactory.getLogger(classOf[ClassNameTypeHints])

  val notContainClazz = Set[Class[_]](classOf[List[_]],classOf[Set[_]],classOf[Map[_,_]])
  override def containsHint(clazz: Class[_]): Boolean = {
    if(clazz.isArray){
      false
    }else{
      !notContainClazz.contains(clazz)
    }
  }

  def hintFor(clazz: Class[_]): String = clazz.getName

  def classFor(hint: String): Option[Class[_]] = try{
    Some(Class.forName(hint))
  }catch{
    case e : ClassNotFoundException => {
      logger.warn(hint + " not found.Fail to deserialize cache",e)
      None
    }
    case e : Throwable => {
      logger.warn("Unknown error.Fail to deserialize cache.",e)
      None
    }
  }
}

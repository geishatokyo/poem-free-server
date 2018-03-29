package com.geishatokyo.apollon.util.json

import org.json4s.TypeHints

/**
 * Created by yamaguchi on 2015/05/28.
 */
trait BaseTypeHints extends TypeHints{
  val generatedClasses: List[Class[_]]
  val classToHint : Map[Class[_],String]
  val hintToClass  : Map[String,Class[_]]

  override def hintFor(clazz: Class[_]): String = {
    val result = classToHint.getOrElse(clazz,"unknown")
    result
  }

  override def classFor(hint: String): Option[Class[_]] =
    hintToClass.get(hint)
}

package moxiu.com.server.recommender

import java.util.concurrent.ConcurrentHashMap
import scala.beans.BeanProperty
import java.util.concurrent.LinkedBlockingQueue

object SelfMap {
  // 装载无算法推荐队列
  val selfPublicMap = new ConcurrentHashMap[String, Array[String]]()
  // 更新Hbase index
  val putQueue = new LinkedBlockingQueue[(String, List[(String, String, String, String)])]()
  // 备用队列
  @BeanProperty var themes_pub = Array[String]()
}
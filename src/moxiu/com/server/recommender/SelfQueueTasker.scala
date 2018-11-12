package moxiu.com.server.recommender

import moxiu.com.server.util.hbase.HbaseTool
import java.text.SimpleDateFormat

/**
 * @author 宿荣全
 * @date 2018-11-07
 * 独立线程每5分钟取一次无算法对列
 */
class SelfQueueTasker extends Runnable {
  val theme_separator = "\002"
  val date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  def run = {
    while (true) {
      try {
        // 无算法取推荐列表
        val pubList = HbaseTool.getRowData("ar:recommend_public", "public")
        // （推荐类型，推荐列表） Map[top,single,cp,train,topic,themes_pub,对应的List]
        pubList.map(info => {
          // 备用队列
          if (info._3 == "themes_pub") SelfMap.setThemes_pub(info._4.split(theme_separator))
          else SelfMap.selfPublicMap.put(info._3, info._4.split(theme_separator))
        })
      } catch {
        case e: Exception => "无算法取推荐列表从hbase取值有问题【SelfQueueTasker.scala】" + e.printStackTrace()
      }
      Thread.sleep(30 * 60 * 1000)
    }
  }
}
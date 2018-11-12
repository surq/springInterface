package moxiu.com.server.recommender

import moxiu.com.server.util.hbase.HbaseTool

/**
 * @author 宿荣全
 * @date 2018-11-07
 * 统一更新hbase的下标
 */
class UpdateHbaseTasker extends Runnable {
  def run = {
    while (true) {
      try {
        val date = SelfMap.putQueue.take()
        HbaseTool.putBatchData(date._1, date._2)
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }
  }
}
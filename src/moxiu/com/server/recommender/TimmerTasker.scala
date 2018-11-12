package moxiu.com.server.recommender

class TimmerTasker extends Runnable {
  def run = {
    // 1、启动读取Hbase线程
    (new Thread(new SelfQueueTasker)).start
    // 2、启动更新Hbase线程
    (new Thread(new UpdateHbaseTasker)).start
  }
}
package moxiu.com.server.MainFram

import org.springframework.boot.SpringApplication
import moxiu.com.server.recommender.TimmerTasker

/**
 * @author 宿荣全
 * @date 2018-11-06
 * spring boot 2.0.6.RELEASE 启动入口
 */
object ApplicationRunner {
  def main(args: Array[String]) {
    SpringApplication.run(classOf[AppConf])
    // 公共队列调起
//    (new Thread(new TimmerTasker)).start
  }
}
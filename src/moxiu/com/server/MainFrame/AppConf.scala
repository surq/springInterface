package moxiu.com.server.MainFram

import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * @author 宿荣全
 * @date 2018-11-06
 * scanBasePackages: 存放各个项目的RestController包路径
 * 每个新接口的包路径都需要在此配置，否则将找不到mapping
 */
@SpringBootApplication(scanBasePackages = Array(
  "moxiu.com.server.recommender"
  ,"moxiu.com.server.others"
  ,"moxiu.com.server.customizer"
  ))
class AppConf 
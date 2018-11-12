package moxiu.com.server.MainFrame

import org.apache.catalina.connector.Connector
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer
import org.springframework.context.annotation.{Bean,Configuration}


/**
 * 自定义tomcat 配置
 * @version
 * @author 宿荣全
 * @date 2018-11-09
 */
@Configuration
class SurqTomcatConnectionCustomizer extends TomcatConnectorCustomizer {
  Console println "==============================sfsfsfsdfsdf=============================="
//  @Value("${spring.server.port}")
//  var port = ""
//  @Value("${spring.server.acceptorThreadCount}")
//  var acceptorThreadCount = ""
//  @Value("${spring.server.minSpareThreads}")
//  var minSpareThreads = ""
//  @Value("${spring.server.maxSpareThreads}")
//  var maxSpareThreads = ""
//  @Value("${spring.server.maxThreads}")
//  var maxThreads = ""
//  @Value("${spring.server.maxConnections}")
//  var maxConnections = ""
//  @Value("${spring.server.connectionTimeout}")
//  var connectionTimeout = ""
//  @Value("${spring.server.protocol}")
//  var protocol = ""
//  @Value("${spring.server.redirectPort}")
//  var redirectPort = ""
//  @Value("${spring.server.compression}")
//  var compression = ""

  //
  //  @Value("${spring.server.MaxFileSize}")
  //  var MaxFileSize = ""
  //  @Value("${spring.server.MaxRequestSize}")
  //  var MaxRequestSize = ""

  @Override
  def customize(connector: Connector) {
    Console println "===============================SurqTomcatConnectionCustomizer  start=============================="
    connector.setPort(8009)
    //    connector.setAttribute("connectionTimeout", connectionTimeout)
    //    connector.setAttribute("acceptorThreadCount", acceptorThreadCount)
    //    connector.setAttribute("minSpareThreads", minSpareThreads)
    //    connector.setAttribute("maxSpareThreads", maxSpareThreads)
    //    connector.setAttribute("maxThreads", maxThreads)
    //    connector.setAttribute("maxConnections", maxConnections)
    //    connector.setAttribute("protocol", protocol)
    //    connector.setAttribute("redirectPort", "redirectPort")
    //    connector.setAttribute("compression", "compression")
    Console println "===============================SurqTomcatConnectionCustomizer  end=============================="
  }
}
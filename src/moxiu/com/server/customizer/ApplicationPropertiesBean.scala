package moxiu.com.server.customizer

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

/**
 * @author 宿荣全
 * date: 2018-11-13
 * 读取Application.Properties文件中定义的参数
 */
@Configuration
class ApplicationPropertiesBean {

  // 是否配置 添加定制Tomcat连接器
  @Value("${tomcat.connector.customizer.flg}")
  var tomcat_flg: String = _
  @Value("${spring.server.port}")
  var port: String = _
  @Value("${spring.server.acceptorThreadCount}")
  var acceptorThreadCount: String = _
  @Value("${spring.server.minSpareThreads}")
  var minSpareThreads: String = _
  @Value("${spring.server.maxSpareThreads}")
  var maxSpareThreads: String = _
  @Value("${spring.server.maxThreads}")
  var maxThreads: String = _
  @Value("${spring.server.maxConnections}")
  var maxConnections: String = _
  @Value("${spring.server.connectionTimeout}")
  var connectionTimeout: String = _
  @Value("${spring.server.protocol}")
  var protocol: String = _
  @Value("${spring.server.redirectPort}")
  var redirectPort: String = _
  @Value("${spring.server.compression}")
  var compression: String = _
  @Value("${spring.server.MaxFileSize}")
  var maxFileSize: String = _
  @Value("${spring.server.MaxRequestSize}")
  var maxRequestSize: String = _
  // =================附加的Tomcat连接器==================
  // 是否配置 附加的Tomcat连接器
  @Value("${additional.tomcat.connectors.flg}")
  var additional_flg: String = _
  // Tomcat监听多个端口
  @Value("${spring.server.port.list}")
  var portList: String = _
}
package moxiu.com.server.customizer

import org.apache.catalina.connector.Connector
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer
import org.springframework.context.annotation.Configuration
import org.apache.coyote.http11.Http11NioProtocol
import org.springframework.beans.factory.annotation.Autowired

/**
 * 自定义tomcat 配置
 * @version
 * @author 宿荣全
 * date 2018-11-09
 *  注意此类放在moxiu.com.server.MainFram包下将不能注入spring boot
 *  此处使用{@Component}或{@Configuration} 都可以
 */
@Configuration
class SurqTomcatConnectionCustomizer extends TomcatConnectorCustomizer {

  // 注入Application.Properties文件中定义的参数
  @Autowired
  val propertiesBean: ApplicationPropertiesBean = null

  // 配置定制Tomcat连接器
  @Override
  def customize(connector: Connector) {
    connector.setPort(propertiesBean.port.toInt)
    Console println "================定制Tomcat连接器开始监听端口:" + propertiesBean.port
    setTomcatAttribute(connector, "connectionTimeout", propertiesBean.connectionTimeout, "Int")
    setTomcatAttribute(connector, "connectionTimeout", propertiesBean.acceptorThreadCount, "Int")
    setTomcatAttribute(connector, "connectionTimeout", propertiesBean.minSpareThreads, "Int")
    setTomcatAttribute(connector, "connectionTimeout", propertiesBean.maxSpareThreads, "Int")
    setTomcatAttribute(connector, "connectionTimeout", propertiesBean.maxThreads, "Int")
    setTomcatAttribute(connector, "connectionTimeout", propertiesBean.maxConnections, "Int")
    setTomcatAttribute(connector, "connectionTimeout", propertiesBean.protocol, "String")
    setTomcatAttribute(connector, "redirectPort", propertiesBean.redirectPort, "Int")
    setTomcatAttribute(connector, "compression", propertiesBean.compression, "String")
  }
  /**
   * 设置Tomcat属性参数
   * valueType：类弄为“Int”的奖转换为数据，其它类型为String
   */
  def setTomcatAttribute(connector: Connector, name: String, value: String, valueType: String) =
    if (value.trim != "") connector.setAttribute(name, if (valueType == "Int") value.trim.toInt else value.trim)
  /**
   * 附加的Tomcat连接器
   * 可配置Tomcat监听多个端口
   */
  def createSslConnector(port: Int) = {

    val connector = new Connector("org.apache.coyote.http11.Http11NioProtocol")
    val protocol = connector.getProtocolHandler().asInstanceOf[Http11NioProtocol]
    try {
      connector.setPort(port)
      Console println "================附加的Tomcat连接器开始监听端口:" + port
      //      val keystore = new ClassPathResource("keystore").getFile()
      //      val truststore = new ClassPathResource("keystore").getFile()
      //      connector.setScheme("https")
      //      connector.setSecure(true)
      //      protocol.setSSLEnabled(true)
      //      protocol.setKeystoreFile(keystore.getAbsolutePath())
      //      protocol.setKeystorePass("changeit")
      //      protocol.setTruststoreFile(truststore.getAbsolutePath())
      //      protocol.setTruststorePass("changeit")
      //      protocol.setKeyAlias("apitester")
    } catch {
      case ex: Exception => ex.printStackTrace()
    }
    connector
  }
}
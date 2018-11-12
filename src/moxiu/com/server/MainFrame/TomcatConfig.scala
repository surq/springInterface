package moxiu.com.server.MainFrame

import java.io.IOException

import org.apache.catalina.connector.Connector
import org.apache.coyote.http11.Http11NioProtocol
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.context.annotation.{Bean,Configuration}
import org.springframework.core.io.ClassPathResource
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * 建立配置类
 * @author 宿荣全
 * @date 2018-11-09
 * 定制tomcat 容器
 */
@Configuration
class TomcatConfig {
  @Bean
  def getTomcat() = {
    val tomcat = new TomcatServletWebServerFactory()
      Console println "==============================TomcatConfig=============================="
    //  添加定制器
    tomcat.addConnectorCustomizers(new SurqTomcatConnectionCustomizer())
    //    tomcat.addAdditionalTomcatConnectors(createSslConnector())
    tomcat
  }

  //  @Bean
  //  def multipartConfigElement() {
  //    val factory = new MultipartConfigFactory();
  //    //  单个数据大小
  //    factory.setMaxFileSize(MaxFileSize) // KB,MB
  //    /// 总上传数据大小
  //    factory.setMaxRequestSize(MaxRequestSize)
  //    factory.createMultipartConfig()
  //  }

  /**
   *  http连接
   */
  def createSslConnector() = {
    val connector = new Connector("org.apache.coyote.http11.Http11NioProtocol")
    val protocol = connector.getProtocolHandler().asInstanceOf[Http11NioProtocol]
    try {
      val keystore = new ClassPathResource("keystore").getFile()
      val truststore = new ClassPathResource("keystore").getFile()
      connector.setScheme("https");
      connector.setSecure(true);
      connector.setPort(8443);
      protocol.setSSLEnabled(true);
      protocol.setKeystoreFile(keystore.getAbsolutePath());
      protocol.setKeystorePass("changeit");
      protocol.setTruststoreFile(truststore.getAbsolutePath());
      protocol.setTruststorePass("changeit");
      protocol.setKeyAlias("apitester");
    } catch {
      case ex: IOException =>
        throw new IllegalStateException("can't access keystore: [" + "keystore"
          + "] or truststore: [" + "keystore" + "]", ex);
    }
    connector
  }

}
package moxiu.com.server.customizer

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.context.annotation.{ Bean, Configuration }
import org.springframework.beans.factory.annotation.Autowired

/**
 * 建立配置类
 * @author 宿荣全
 * @date 2018-11-09
 * 定制tomcat 容器 注意要加入注解{@Configuration}
 */
@Configuration
class TomcatConfig {
  
	// spring boot 装载定制Tomcat连接器
  @Autowired
  val tomcatConnector: SurqTomcatConnectionCustomizer = null

    // 注入Application.Properties文件中定义的参数
  @Autowired
  val propertiesBean: ApplicationPropertiesBean = null

  @Bean
  def servletContainer() = {
    val tomcat = new TomcatServletWebServerFactory
    //  添加定制Tomcat连接器
    if (propertiesBean.tomcat_flg.toBoolean) tomcat.addConnectorCustomizers(tomcatConnector)
    //  配置 附加的Tomcat连接器
    if (propertiesBean.additional_flg.toBoolean) propertiesBean.portList.split(",").map(port => tomcat.addAdditionalTomcatConnectors(tomcatConnector.createSslConnector(port.toInt)))
    tomcat
  }
//    @Bean
//    def multipartConfigElement() {
//      val factory = new MultipartConfigFactory();
//      //  单个数据大小
//      factory.setMaxFileSize(MaxFileSize) // KB,MB
//      /// 总上传数据大小
//      factory.setMaxRequestSize(MaxRequestSize)
//      factory.createMultipartConfig()
//    }

}
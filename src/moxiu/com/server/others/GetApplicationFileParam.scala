package moxiu.com.server.others

import org.springframework.web.bind.annotation.{ RequestMapping, RestController }
import org.springframework.beans.factory.annotation.Value

/**
 * @author 宿荣全
 * 通过注解@Value 获取Application.properties中定义的变量
 * 注意：
 * 通过注解@Value 获取时必需是类变量，写在方法中无法加载
 */
@RestController
class GetApplicationFileParam {
  @Value("${app.name}")
  var app_Name: String = _
  @Value("${moxiu.author}")
  var author: String = _
  @RequestMapping(Array("/getParam", "/test_value"))
  def fun() = "通过注解@Value 获取Application.properties中定义的变量：app.name=" + app_Name + "，moxiu.author" + author
}
package moxiu.com.server.others

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.beans.factory.annotation.Value
import scala.beans.BeanProperty


@RestController
class stt {
    @RequestMapping(Array("/aaaa", "/bbb"))
  def fun()={
        @Value("${spring.server.port}")
  @BeanProperty var port:String = ""
   "surongquan!!!!" +port+"]"
    }
}
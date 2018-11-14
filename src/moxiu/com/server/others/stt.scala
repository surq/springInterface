package moxiu.com.server.others

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.beans.factory.annotation.Value

@RestController
class stt {
  @RequestMapping(Array("/aaaa"))
  def fun() = "surongquan!!!!"
}

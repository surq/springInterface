package moxiu.com.server.recommender

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam
import scala.collection.mutable.Map
import com.alibaba.fastjson.JSONObject

/**
 * @author 宿荣全
 * @date 2018-11-07
 * 主题管家个性化推荐接口转接
 */
@RestController
class RecommenderController {

  @RequestMapping(Array("/recommend"))
  def calculate(
    @RequestParam(required = true, defaultValue = "") uid:        String,
    @RequestParam(required = false, defaultValue = "0") topic:    Int,
    @RequestParam(required = false, defaultValue = "0") theme_top:Int,
    @RequestParam(required = false, defaultValue = "0") theme_sim:Int,
    @RequestParam(required = false, defaultValue = "0") theme_extend:Int,
    @RequestParam(required = false, defaultValue = "0") theme_profile:Int,
    @RequestParam(required = false, defaultValue = "0") single:   Int,
    @RequestParam(required = false, defaultValue = "0") cp:       Int,
    @RequestParam(required = false, defaultValue = "0") train:    Int) = {
    val map = Map[String, Int]()
    map += ("topic" -> topic)
    map += ("top" -> theme_top)
    map += ("simuser" -> theme_sim)
    map += ("extend" -> theme_extend)
    map += ("profile" -> theme_profile)
    map += ("single" -> single)
    map += ("cp" -> cp)
    map += ("train" -> train)
    val resultMap = (new ThemeTask()).runTask(uid, map)
    //更名
    val aList = List("top", "profile", "extend")
    val bList = List("simuser")
    val jsonFormat = resultMap.map(info => {
      val topic = if (aList.contains(info._1)) "theme_" + info._1
      else if (info._1 == "simuser") "theme_sim" else info._1
      val list = info._2.map(themid => {
        val jsonObj = new JSONObject()
        jsonObj.put("id", themid)
        jsonObj
      })
      (topic -> list)
    })
    // 返回json
    val jsonObj = new JSONObject()
    jsonFormat.map(kv => jsonObj.put(kv._1, kv._2))
    jsonObj
  }
}
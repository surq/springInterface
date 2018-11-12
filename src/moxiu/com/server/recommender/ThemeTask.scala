package moxiu.com.server.recommender

import scala.collection.mutable.{ ArrayBuffer, Map }
import scala.collection.JavaConverters._
import moxiu.com.server.util.hbase.HbaseTool

/**
 * @author 宿荣全
 * @date 2018-11-07
 * 主题管家个性化推荐列表取得
 */
class ThemeTask {
  /**
   * user：muuid
   * map [推荐类型，个数]
   */
  def runTask(user: String, map: Map[String, Int]) = {
    val theme_separator = "\002"
    // 无算法取值
    val takeSelfNameList = Array(map.get("top").get, map.get("single").get, map.get("cp").get, map.get("train").get, map.get("topic").get)
    // 有算法取值
    val takeAlgorithNameList = Array(map.get("profile").get, map.get("simuser").get, map.get("extend").get)
    val Array(profile_takeN, simUser_takeN, extend_takeN) = takeAlgorithNameList
    // 取值
    if ((takeSelfNameList ++ takeAlgorithNameList).sum > 0) {
      // 为新用户 欲取所有算法队列总个数
      val takeAlgorith_sum = takeAlgorithNameList.sum
      // 无算法队列
      val selfNameList = List("top", "single", "cp", "train", "topic")
      // 算法队列
      val algorithNameList = List("profile", "simuser", "extend")
      // 无算法取值
      val takeSelfNameMap = selfNameList.zip(takeSelfNameList).toMap
      // 取值列表
      // 实际 索要算法推荐列表
      val algorithNameTakeMap = algorithNameList.map(name => (name, map.get(name).get)).filter(_._2 != 0)
      // 实际 索要无算法推荐列表
      val selfNameTakeMap = selfNameList.map(name => (name, map.get(name).get)).filter(_._2 != 0)

      //-------------------------------------------公共计算部分----------------------------------------------------------
      // 有算法取推荐列表 
      val indList = HbaseTool.getRowData("ar:recommend_list_bulkload", user)
      // 备用队列
      val themes_pub = SelfMap.getThemes_pub
      // 无算法推荐列表
      val pub_List = SelfMap.selfPublicMap

      //-------------------------------------------用户初次访问----------------------------------------------------------
      // 用户初次访问
      val result_value = if (indList.size == 0) {
        // 有算法
        // 有算法（用户画像，相似用户，扩展标签）的推荐列表
        // 计算算法队列的下标值
        val end_simUser = profile_takeN + simUser_takeN
        val end_extend = end_simUser + extend_takeN
        // 1、定义新用户,算法推荐列表的取值范围 过滤掉不取的
        val algorithmIndexMap = Map("profile" -> (0, profile_takeN), "simuser" -> (profile_takeN, end_simUser),
          "extend" -> (end_simUser, end_extend)).filter(f => f._2._1 != f._2._2).toList
        // 1、推荐列表
        val algorithm_RM_List = algorithmIndexMap.map(kv => (kv._1, getThemeIdList(themes_pub, kv._2)._2))
        //无算法
        // 3-4、无算法推荐列表队列生成、更新历史索引
        val take_pub_List = takeSelfNameMap.map(kv => (kv._1, getThemeIdList(pub_List.get(kv._1), (0, kv._2))))

        //(无算法推荐列表,更新历史索引)
        val resuls = take_pub_List.map(e => { ((e._1, e._2._2), (e._1, e._2._1)) })
        val pub_RM_List = resuls.map(_._1).toList
        // 2、更新算法推荐历史索引 新用户标签
        val pub_index_List = ("themes_pub", takeAlgorith_sum) :: resuls.map(_._2).toList
        val recommenderList = algorithm_RM_List ++ pub_RM_List.filter(_._2.size != 0)
        // 2、更新算法推荐历史索引 新用户标签
        val recommender_index_List = ("themes_pub", takeAlgorith_sum) :: pub_index_List
        (recommenderList, recommender_index_List)
      } else {
        //-------------------------------------------用户二次访问----------------------------------------------------------
        // 用户二次访问
        // 有算法（用户画像，相似用户，扩展标签）的推荐列表
        val algorithm_Queue_List = ArrayBuffer[(String, List[String])]()
        // 所有推荐的历史下标值
        val historyIndexList = ArrayBuffer[(String, Int)]()
        //1、 两个列族的数据分开
        indList.map(info => (info._2, info._3, info._4)).foreach(line =>
          if (line._1 == "pushing") algorithm_Queue_List += ((line._2, line._3.split(theme_separator).toList)) else historyIndexList += ((line._2, line._3.toInt)))
        //(类型，取值个数，[开始下标，结束下标])左闭右开区间
        val historyIndexMap = historyIndexList.toMap
        //-------------------------------------------无算法推荐计算----------------------------------------------------------
        val t2 = if (selfNameTakeMap.size > 0) {
          // 2、无算法推荐列表队列生成、更新算法推荐历史索引
          val selfIndexInfoMap = selfNameTakeMap.map(info => (info._1, (historyIndexMap(info._1), historyIndexMap(info._1) + info._2))).toMap
          val take_pub_List = selfNameTakeMap.map(kv => (kv._1, getThemeIdList(pub_List.get(kv._1), selfIndexInfoMap.get(kv._1).get)))
          // 无算法推荐结果
          val self_resuts = take_pub_List.map(info => (((info._1, info._2._1), (info._1, info._2._2))))
          //(无算法推荐列表 更新index，更新算法推荐历史索引)
          (self_resuts.map(_._2), self_resuts.map(_._1))
        } else (ArrayBuffer[(String, List[String])](), ArrayBuffer[(String, Int)]())
        //-------------------------------------------有算法推荐计算----------------------------------------------------------
        val t1 = if (algorithNameTakeMap.size > 0) {
          // 有算法推荐列表 更新index
          // 算法推荐列表队列生成、推荐列表
          //-------------------------------------------新用户二次访问----------------------------------------------------------
          // 新用户
          if (algorithm_Queue_List.size == 0) {
            // simUser,profile,extend 对新用户这三种的历史index 是一样的
            val historyIndex = ((historyIndexMap("themes_pub")) + takeAlgorith_sum) % themes_pub.size
            // 定义新用户,算法推荐列表的取值范围
            val takeList = algorithNameTakeMap.map(_._2).scanLeft(historyIndex)(_ + _)
            val headList = takeList.take(takeList.size - 1)
            val tailList = takeList.drop(1)
            val algorithmIndexMap = algorithNameTakeMap.map(_._1).zip(headList.zip(tailList)).toMap
            // 1、更新算法推荐历史索引 2、推荐列表
            val rm_list = algorithmIndexMap.map(info => (info._1, getThemeIdList(themes_pub, algorithmIndexMap.get(info._1).get)._2)).toList
            (rm_list, List(("themes_pub", historyIndex + takeAlgorith_sum)))
          } else {
            //-------------------------------------------老用户二次访问----------------------------------------------------------
            // 老用户
            // 算法推荐列表队列生成
            // 1、推荐列表、更新算法推荐历史索引
            val algorithm_Queue_Map = algorithm_Queue_List.toMap
            val indexInfoMap = algorithNameTakeMap.map(info => (info._1, (historyIndexMap(info._1), historyIndexMap(info._1) + info._2))).toMap
            val rm_list = algorithNameTakeMap.map(info => (info._1, getThemeIdList(algorithm_Queue_Map(info._1).toArray, indexInfoMap(info._1))))
              .map(info => ((info._1, info._2._2), (info._1, info._2._1)))
            (rm_list.map(_._1), rm_list.map(_._2))
          }
        } else (List[(String, List[String])](), List[(String, Int)]())
        val recommenderList = t1._1 ++ t2._1
        val recommender_index_List = t1._2 ++ t2._2
        (recommenderList, recommender_index_List)
      }
      // 更新历史下标索引
      val update_indexList = result_value._2.map(line => (user, "pushed", line._1, line._2.toString)).toList
      if (update_indexList.size > 0) SelfMap.putQueue.offer(("ar:recommend_list_bulkload", update_indexList))
      // 返回结果集 过滤掉为0的
      result_value._1.filter(line => if (map.get(line._1).get == 0) false else true).map(line => (line._1, line._2.toArray)).toMap
    } else {
      Map[String, Array[String]]()
    }
  }

  /**
   * 返回当前游标和所取列表
   */
  def getThemeIdList(themeId_list: Array[String], takeRagen: (Int, Int)) = {
    val listSize = themeId_list.size
    val valueList = for (index <- takeRagen._1 until takeRagen._2) yield themeId_list(index % listSize)
    val currentIndex = takeRagen._2 % listSize
    (currentIndex, valueList.toList)
  }
}
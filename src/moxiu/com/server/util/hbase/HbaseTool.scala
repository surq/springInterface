package moxiu.com.server.util.hbase
import org.apache.hadoop.conf.Configuration
import scala.collection.mutable.ArrayBuffer
import java.util.ArrayList
import org.apache.hadoop.hbase.{ HBaseConfiguration, HColumnDescriptor, HTableDescriptor, TableName }
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes
import scala.collection.JavaConverters._
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.ByteArrayOutputStream

case class HbaseToolCase(act_type: String, themeId: String, muuid: String)
/**
 * @author 宿荣全
 * @date 2018-09-26
 * @comment 读取/写入Hbase的API封装工具
 */
object HbaseTool {

  val conf = getHbaseConf
  // hbase配置
  /**
   * 创建hbase连接
   * 如果做接口的话，要注意是静态的，做为spring 的connecter不然会很慢
   */
  var connecter = ConnectionFactory.createConnection(conf)
  var admin = connecter.getAdmin

  /**
   * @comment
   * <br>使用HbaseTool完后需调用此方法否则连接不释放</br>
   * <br>关闭连接</br>
   */
  def toolClose = {
    if (admin != null) admin.close
    if (connecter != null) connecter.close
  }

  /**
   * 连接hbase的配置
   */
  def getHbaseConf: Configuration = {
    val hbaseConfiguration = HBaseConfiguration.create
    //    hbaseConfiguration.addResource("./linuxshare/src/conf/hbase-site.xml")
    hbaseConfiguration.set("hbase.zookeeper.property.clientPort", "2181")
    hbaseConfiguration.set("hbase.zookeeper.quorum", "10.1.0.180,10.1.0.153,10.1.0.150,10.1.0.235,10.1.0.166")
    // 重试3次，目前集群配置为35次
    hbaseConfiguration.set("hbase.client.retries.number", "3")
    // 重试3次，时间间隔
    hbaseConfiguration.set("hbase.client.pause", "50")
    hbaseConfiguration.set("hbase.rpc.timeout", "5000")
    hbaseConfiguration.set("hbase.client.operation.timeout", "5000")
    hbaseConfiguration.set("hbase.client.scanner.timeout.period", "10000")
    hbaseConfiguration
  }
  //==========================创建hbase表============================================
  /**
   * 创建hbase表并带多个列族
   */
  def createTable(tbname: String, columnFamilys: Array[String]): Boolean = {
    val admin = new HBaseAdmin(conf)
    var tableExists_flg = false
    if (admin.tableExists(tbname)) {
      Console println tbname + "表已存在！"
      tableExists_flg = true
    } else {
      //创建Hbase表模式
      val descriptor = new HTableDescriptor(tbname)
      //创建各列簇
      for (columnFamily <- columnFamilys) descriptor.addFamily(new HColumnDescriptor(columnFamily))
      admin.createTable(descriptor)
      tableExists_flg = admin.tableExists(tbname)
      Console println tbname + "创建完毕！"
    }
    if (admin != null) admin.close
    tableExists_flg
  }

  //==========================向Hbase 中插入数据============================================
  /**
   * 向hbase表中插入一条数据
   */
  def putData(tableName: String, rowKey: String, column: String, item: String, value: String) = putCore(tableName, getDataPuter(rowKey, column, item, value))
  /**
   * 向hbase表中插入一条带时间版本数据
   */
  def putData(tableName: String, rowKey: String, column: String, item: String, timestamp: Long, value: String) = putCore(tableName, getVersionDataPuter(rowKey, column, item, timestamp, value))
  /**
   * 向hbase表中批量插入数据
   * valueList ：(rowKey, column, item, value)
   */
  def putBatchData(tableName: String, valueList: List[(String, String, String, String)]) = putCore(tableName, (getdataListPuter(valueList).asScala): _*)

  /**
   * 根据rowKey,列族、列 写入 Object 类型数据
   */
  def putObj(tableName: String, rowKey: String, column: String, item: String, obj: Any) = putCore(tableName, getObjPuter(rowKey, column, item, obj))

  /**
   * 向hbase表中批量插入Object 类型数据
   * valueList ：(rowKey, column, item, value)
   */
  def putBatchObj(tableName: String, valueList: List[(String, String, String, Any)]) = putCore(tableName, (getObjListPuter(valueList).asScala): _*)

  /**
   * 获取向hbase表中插入数据的puter
   */
  private def getDataPuter(rowKey: String, column: String, item: String, value: String) = {
    val put = new Put(Bytes.toBytes(rowKey))
    put.addColumn(Bytes.toBytes(column), Bytes.toBytes(item), Bytes.toBytes(value))
    put
  }

  /**
   * 获取向hbase表中插入数据的puter
   */
  private def getVersionDataPuter(rowKey: String, column: String, item: String, timestamp: Long, value: String) = {
    val put = new Put(Bytes.toBytes(rowKey))
    put.addColumn(Bytes.toBytes(column), Bytes.toBytes(item), timestamp, Bytes.toBytes(value))
    put
  }

  /**
   * 向hbase表中批量插入数据的puter
   * valueList ：(rowKey, column, item, value)
   */
  private def getdataListPuter(valueList: List[(String, String, String, String)]) = {
    val putList = new ArrayList[Put]()
    valueList.map(line => {
      val rowKey = line._1
      val column = line._2
      val item = line._3
      val value = line._4
      val put = new Put(Bytes.toBytes(rowKey))
      put.addColumn(Bytes.toBytes(column), Bytes.toBytes(item), Bytes.toBytes(value))
      putList.add(put)
    })
    putList
  }

  /**
   * 向hbase表中批量插入数据的puter
   * valueList ：(rowKey, column, item, value)
   */
  private def getObjListPuter(valueList: List[(String, String, String, Any)]) = {
    val putList = new ArrayList[Put]()
    valueList.map(line => {
      val rowKey = line._1
      val column = line._2
      val item = line._3
      val obj = line._4
      val put = new Put(Bytes.toBytes(rowKey))
      put.addColumn(Bytes.toBytes(column), Bytes.toBytes(item), objToByteArray(obj))
      putList.add(put)
    })
    putList
  }

  /**
   * object puter 句柄
   */
  private def getObjPuter(rowKey: String, column: String, item: String, obj: Any) = {
    val puter = new Put(Bytes.toBytes(rowKey))
    puter.addColumn(Bytes.toBytes(column), Bytes.toBytes(item), objToByteArray(obj))
    puter
  }
  /**
   * put 数据
   */
  private def putCore(tableName: String, puters: Put*) = {
    val table = connecter.getTable(TableName.valueOf(tableName))
    if (puters.size == 1) table.put(puters(0)) else table.put(puters.asJava)
    // 执行table-close 会自动提交一次
    if (table != null) table.close
  }
  //==========================从hbase表中取数据============================================
  /**
   * 取单个rowKey数据
   */
  def getRowData(tableName: String, rowKey: String) = getRowValue(tableName, rowKey)
  /**
   * 取多个rowKey数据
   */
  def getBatchRowData(tableName: String, rowKeyList: List[String]) = getRowValue(tableName, rowKeyList: _*)
  /**
   * @param infoList:List[(rowKey,family,item)]
   * 通过rowKey,列族,列 批量取数据
   */
  def getBatchData(tableName: String, getinfoList: List[(String, String, String)]) = getItemValue(false, tableName, getinfoList: _*)
  /**
   * @param getinfo：(rowKey,family,item)
   * 通过rowKey,列族,列 取单条数据
   */
  def getData(tableName: String, getinfo: (String, String, String)) = getItemValue(false, tableName, getinfo)
  /**
   * @param infoList:List[(rowKey,family,item)]
   * 通过rowKey,列族,列 批量取Obj类型数据
   */
  def getBatchObj(tableName: String, getinfoList: List[(String, String, String)]) = getItemValue(true, tableName, getinfoList: _*)
  /**
   * @param getinfo：(rowKey,family,item)
   * 通过rowKey,列族,列 取单条Obj类型数据
   */
  def getObj(tableName: String, getinfo: (String, String, String)) = getItemValue(true, tableName, getinfo)

  /**
   * 根据rowKey取数据
   */
  private def getRowValue(tableName: String, rowKeyList: String*) = {
    val getList = new ArrayList[Get]()
    rowKeyList.map(rowKey => getList.add(new Get(Bytes.toBytes(rowKey))))
    // rowKey,列族,列,值
    getCore(tableName, getList)
  }
  /**
   * 根据rowKey,列族、列 取数据
   */
  private def getItemValue(isObj_flg: Boolean, tableName: String, getinfoList: (String, String, String)*) = {
    val getList = new ArrayList[Get]()
    // 列族,列,值
    getinfoList.map(info => getList.add(new Get(Bytes.toBytes(info._1)).addColumn(Bytes.toBytes(info._2), Bytes.toBytes(info._3))))
    if (isObj_flg) getObjCore(tableName, getList) else getCore(tableName, getList)
  }

  private def getCore(tableName: String, getList: ArrayList[Get]) = {
    // 列族,列,值
    val resultList = ArrayBuffer[(String, String, String, String)]()
    val table = connecter.getTable(TableName.valueOf(tableName))
    if (getList.size == 1) {
      val result = table.get(getList.get(0))
      // 查询结果[rowKey,列族,列,值],（result.raw过时改为result.rawCells）
      result.raw.foreach(f => resultList += ((Bytes.toString(f.getRow), Bytes.toString(f.getFamily), Bytes.toString(f.getQualifier), Bytes.toString(f.getValue))))
    } else {
      table.get(getList).map(result => {
        // 查询结果[rowKey,列族,列,值],（result.raw过时改为result.rawCells）
        result.raw.foreach(f => resultList += ((Bytes.toString(f.getRow), Bytes.toString(f.getFamily), Bytes.toString(f.getQualifier), Bytes.toString(f.getValue))))
      })
    }
    if (table != null) table.close
    resultList.toList
  }

  private def getObjCore(tableName: String, getList: ArrayList[Get]) = {
    // 列族,列,值
    val resultList = ArrayBuffer[(String, String, String, Any)]()
    val table = connecter.getTable(TableName.valueOf(tableName))
    table.get(getList).map(result => {
      // 查询结果[rowKey,列族,列,值],（result.raw过时改为result.rawCells）
      result.raw.foreach(f => resultList += ((Bytes.toString(f.getRow), Bytes.toString(f.getFamily), Bytes.toString(f.getQualifier), byteArrayToObj(f.getValue))))
    })
    table.close
    resultList.toList
  }
  //==========================从hbase表中删除数据============================================
  /**
   * 删除行
   */
  def delete(tableName: String, rowKey: String) = deleCore(tableName, rowDele(rowKey))
  /**
   * 删除列族
   */
  def delete(tableName: String, rowKey: String, family: String) = deleCore(tableName, familyDele(rowKey, family))
  /**
   * 删除列
   */
  def delete(tableName: String, rowKey: String, family: String, colum: String) = deleCore(tableName, columDele(rowKey, family, colum))

  private def rowDele(rowKey: String) = new Delete(Bytes.toBytes(rowKey))
  private def familyDele(rowKey: String, family: String) = {
    val delrow = new Delete(Bytes.toBytes(rowKey))
    //删除指定列族数据
    delrow.addFamily(Bytes.toBytes(family))
    delrow
  }
  private def columDele(rowKey: String, family: String, colum: String) = {
    val delrow = new Delete(Bytes.toBytes(rowKey))
    //删除指定列族的列数据(addColumns:删除所有版本；addColumn：只删除最新版本)
    delrow.addColumns(Bytes.toBytes(family), Bytes.toBytes(colum))
    delrow
  }
  /**
   * 删除数据
   */
  private def deleCore(tableName: String, delrow: Delete) = {
    val table = connecter.getTable(TableName.valueOf(tableName))
    table.delete(delrow)
    table.close
  }
  //-----------------关于detele的解释----------------------
  // 表示删除column family里面所有版本的cells，
  // Delete deleteFamily(byte[] family)
  // 表示只删除column family指定timestamp版本或者所有更老的版本的cells。
  // Delete deleteFamily(byte[] family, long timestamp)
  // 表示删除column qualifier所有版本的cells，
  // Delete deleteColumns(byte[] family, byte[] qualifier)
  // 表示只删除指定timestamp版本或者更老版本的cells。
  // Delete deleteColumns(byte[] family, byte[] qualifier, long timestamp)
  // 表示删除最新版本的cell，
  // Delete deleteColumn(byte[] family, byte[] qualifier)
  // 表示删除指定timestamp的cell
  // Delete deleteColumn(byte[] family, byte[] qualifier, long timestamp)
  // 表示删除指定timestamp或者所有更老版本的cells
  // void setTimestamp(long timestamp)
  /**
   * 从hbase中删除表
   */
  def dropTable(tableName: String) = {
    val htable = TableName.valueOf(tableName)
    if (admin.tableExists(htable)) {
      admin.disableTable(htable)
      admin.deleteTable(htable)
    } else println(tableName + "表不存在！")
  }
  //==========================扫描hbase表============================================
  /**
   * 扫描表 rowKey
   */
  def scanTableRowKey(tableName: String) = {
    val table = connecter.getTable(TableName.valueOf(tableName))
    var scanner: ResultScanner = null
    var result: Result = null
    val keyList = ArrayBuffer[String]()
    try {
      scanner = table.getScanner(new Scan)
      result = scanner.next
      while (result != null) {
        // 取rowKey
        val key = Bytes.toString(result.getRow)
        keyList += key
        // println(s"列簇:${columnFamily}:${column},value:${Bytes.toString(result.getValue(Bytes.toBytes(columnFamily), Bytes.toBytes(column)))}")
        result = scanner.next
      }
    } catch {
      case e: Exception => e.printStackTrace
    } finally {
      //通过scan取完数据后，一定要关闭ResultScanner，否则RegionServer可能会出现问题(对应的Server资源无法释放)
      if (scanner != null) scanner.close
      table.close
    }
    keyList
  }
  /**
   * hbase扫描数据库列表
   */
  def scanHbaseTablesStruct = admin.listTables.foreach(println)

  //  /**
  //   * 根据rowKey,列族、列 写入 Object 类型数据
  //   */
  //  private def putObj(tableName: String, rowKey: String, column: String, item: String, obj: Array[Byte]) {
  //    val table = connecter.getTable(TableName.valueOf(tableName))
  //    val puter = new Put(Bytes.toBytes(rowKey))
  //    puter.addColumn(Bytes.toBytes(column), Bytes.toBytes(item), obj)
  //    table.put(puter)
  //    table.close
  //  }

  //=================object \ByteArray 互转=======================
  /**
   * object 转成ByteArray
   */
  def objToByteArray(obj: Any) = {
    var bytes: Array[Byte] = null
    val bos = new ByteArrayOutputStream
    val oos = new ObjectOutputStream(bos)
    try {
      oos.writeObject(obj)
      oos.flush
      bytes = bos.toByteArray
    } catch {
      case ex: IOException => ex.printStackTrace();
    } finally {
      oos.close
      bos.close
    }
    bytes
  }
  /**
   *  ByteArray转成object
   */
  def byteArrayToObj(bytes: Array[Byte]) = {
    var obj: Any = null
    try {
      val bis = new ByteArrayInputStream(bytes)
      val ois = new ObjectInputStream(bis)
      obj = ois.readObject()
      ois.close
      bis.close
    } catch {
      case e: Exception => e.printStackTrace
    }
    obj
  }
}
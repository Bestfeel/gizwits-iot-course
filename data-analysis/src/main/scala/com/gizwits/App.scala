package com.gizwits

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.json4s.JsonAST._
import org.json4s.jackson.JsonMethods._

/**
  * Created by feel on 2017/7/15.
  */
object App {


  case class Device(product_key: String, mac: String, `type`: String, ts: Long)

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf(true)
      .setAppName("StatisticsAnalysis")
      .set("spark.scheduler.mode", "FAIR")
      .set("spark.default.parallelism", "16")
      .setMaster("local[4]")


    val sc = new SparkContext(conf)

    val localFilePath = "file://data/rtdata.json"

    // 解析字段，提取需要的字段数据
    val dataRDD = sc.textFile(localFilePath, 10)
      .map(line => parse(line))
      .map(line => Device(
        jv2String(line \ "product_key")
        , jv2String(line \ "mac")
        , jv2String(line \ "type")
        , jv2String((line \ "timestamp")).toLong
      ))

    val acticeCmd = List("dev2app", "app2dev")
    val incrCmd = List("dev_online", "dev_offline", "dev_re_online")


    // 统计活跃设备
    countByDevice(dataRDD.filter(d => acticeCmd.contains(d.`type`)), true).foreach(println _)
    // 统计新增(激活)设备
    countByDevice(dataRDD.filter(d => incrCmd.contains(d.`type`)), true).foreach(println _)
  }

  /**
    * 根据pk 指定设备数量
    *
    * @param device
    * @return
    */
  def countByDevice(device: RDD[Device], uniq: Boolean = true): RDD[(String, Long)] = {
    uniq match {
      case true => {
        device.map(d => ((d.product_key, d.mac), d.ts))
          .reduceByKey(Math.max)
          .map {
            case ((pk, mac), ts) => (pk.toString, 1L)
          }.repartition(1).reduceByKey(_ + _)
      }
      case false => {
        device.map(d => ((d.product_key, d.mac), d.ts))
          .reduceByKey(Math.min)
          .map {
            case ((pk, mac), ts) => (pk.toString, 1L)
          }.repartition(1).reduceByKey(_ + _)
      }
    }

  }


  def jv2String(json: JValue): String = {
    import org.json4s._
    implicit lazy val formats = org.json4s.DefaultFormats
    json match {
      case JBool(value) => value.toString
      case JDecimal(value) => value.toString
      case JInt(value) => value.toString
      case JDouble(value) => value.toLong.toString
      case JString(value) => value.toString
      case JNull => ""
      case JNothing => ""
      case jv: JValue => jv.extract[String]
    }
  }

}

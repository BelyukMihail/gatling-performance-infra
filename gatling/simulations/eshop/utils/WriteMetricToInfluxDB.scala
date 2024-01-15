package eshop.utils

import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder

import java.time.{LocalDateTime, ZoneOffset}
import java.util.concurrent.TimeUnit

class WriteMetricToInfluxDB(simulationName: String) {

  val measurementName = "gatling"

  def writeError(influxdb: InfluxDB, requestName: String): ChainBuilder = {
    exec(session => {
      val fieldName = "value"
      val statusCode = session("statusCode").as[Int]
      val responseBody = session("responseBody").as[String]
      val error = s"$statusCode $responseBody"
      writeErrorToInfluxDB(requestName, error, fieldName, influxdb)
      session
    })
  }

  def writeBuildInfo(influxdb: InfluxDB, buildNumber: Int, startTime:LocalDateTime) = {
      val tagName = "buildNumber"
      writeBuildInfoToInflux(buildNumber, startTime, tagName, influxdb)
  }

  private def writeErrorToInfluxDB(requestName: String, fieldValue: String, fieldName: String, influxdb: InfluxDB): Unit = {
    val singleRequest = Point.measurement(measurementName)
      .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
      .tag("testName", simulationName)
      .tag("requestName", requestName)
      .addField(fieldName, fieldValue)
      .build()

    val allRequests = Point.measurement(measurementName)
      .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
      .tag("testName", simulationName)
      .tag("requestName", "allRequests")
      .addField(fieldName, fieldValue)
      .addField("requestValue", requestName)
      .build()

    influxdb.write(singleRequest)
    influxdb.write(allRequests)
  }

  private def writeBuildInfoToInflux(buildNumber: Int, startTime: LocalDateTime, tagName: String, influxdb: InfluxDB): Unit = {
    val currentTimeMs = System.currentTimeMillis()
    val startTimeMs = startTime.toInstant(ZoneOffset.UTC).toEpochMilli

    val point = Point.measurement(measurementName)
      .time(currentTimeMs, TimeUnit.MILLISECONDS)
      .tag("testName", simulationName)
      .tag(tagName, buildNumber.toString)
      .addField("startTime", startTimeMs/1000)
      .addField("finishTime", currentTimeMs/1000)
      .addField("duration", TimeFormatter.formatDuration(currentTimeMs-startTimeMs))
      .build()

    influxdb.write(point)
  }

}
package eshop.utils

import eshop.utils.PropertyConfigurator.getProperty
import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import java.time.{Duration, LocalDateTime}
import java.time.format.DateTimeFormatter

object SlackNotificator {
  private val defaultMessage = """{ "message": "Test started" }"""


  private val grafanaLink = "127.0.0.1:8857/d/gatling/gatling-report-metrics?from=now-5m&refresh=5s"
  private val finishMessage =
    s"""{ "message": "Test started at %s,\n
       |by %s,\n
       |To watch simulation stats in runtime follow the link:\n
       |http://127.0.0.1:8857/d/gatling/gatling-report-metrics?from=now-5m&refresh=5s\n
       |Gatling generated result: (TODO: add this here)"
       |}""".stripMargin

  def requestBody(authorName: String, startDateAndTime: LocalDateTime, endDateAndTime: LocalDateTime) ={
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val formattedDateTime: String = startDateAndTime.format(formatter)
    var testDuration = Duration.between(startDateAndTime, endDateAndTime)
    println(s"start: $startDateAndTime\nend: $endDateAndTime")
    val hours: Long = testDuration.toHours
    testDuration = testDuration.minusHours(hours)
    val minutes: Long = testDuration.toMinutes
    testDuration = testDuration.minusMinutes(minutes)
    val seconds: Long = testDuration.getSeconds

    println(s"seconds: $seconds\nminute: $minutes\nhours: $hours")
    val result =
      s"""{
         |"grafanaLink": "$grafanaLink",
         |"authorName": "$authorName",
         |"startDateAndTime": "$formattedDateTime",
         |"testDuration": "$hours h $minutes m $seconds s"}"""
        .stripMargin
    result
  }


  private val slackDomain = "https://hooks.slack.com/"
  private val channelId = "triggers/T4E815KGA/6273744595475/f6ca30500eb4b1d2761fcccc20f7d99b"

  private val slackProtocol: HttpProtocolBuilder = http
    .baseUrl(slackDomain) // Your Slack Webhook URL

  def sendSlackNotification(): ChainBuilder = doIf(session => {
    session("statusCode").as[Int] != 200
  }) {
    exec(http("slack notification")
      .post(slackDomain + channelId)
      .body(StringBody(defaultMessage)).asJson
    )
  }

  //  def sendNotificationAboutFails(listOfFails: List[(Int, String, String)]): ChainBuilder =
  def sendNotificationAboutFails(): ChainBuilder =
    exec(http("slack notification")
      .post(slackDomain + channelId)
      //      .body(StringBody(listOfFails.toString())).asJson
      .body(StringBody(defaultMessage)).asJson
    )

  def sendSlackNotificationAboutStartChainBuilder() = exec(
    http("Slack Webhook")
      .post(channelId) // Append your actual webhook path
      .body(StringBody(defaultMessage))
      .header("Content-Type", "application/json")
      .check(status.is(200))
  )
  def sendNotificationAboutStart(): ScenarioBuilder = {
    scenario("Start test notification")
      .exec(http("Slack Webhook")
        .post(channelId) // Append your actual webhook path
        .body(StringBody(defaultMessage))
        .header("Content-Type", "application/json")
        .check(status.is(200)))
  }
  //  getProperty("JENKINS_ADMIN_LOGIN", "POKANEADMIN")
  def sendNotificationAboutFinish(startTime: LocalDateTime, endTime: LocalDateTime): ScenarioBuilder = {
    scenario("Finish test notification")
      .exec(http("Slack Webhook")
        .post(channelId) // Append your actual webhook path
        .body(
          StringBody(
            requestBody(
              getProperty("JENKINS_LOGIN", "POKA_NE_ADMIN"),
              startTime,
              endTime
            )
          )
        )
        .header("Content-Type", "application/json")
        .check(status.is(200))
      )
  }

  def getSlackHTTPProtocol: HttpProtocolBuilder = {
    slackProtocol
  }

}
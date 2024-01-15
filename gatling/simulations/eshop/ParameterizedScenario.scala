package eshop

import eshop.chains.OrderCreationChain
import eshop.utils.PropertyConfigurator.getProperty
import eshop.utils.{Configurator, DbClient, ScenarioInjector, SlackNotificator}
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.{PopulationBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import java.time.LocalDateTime

class ParameterizedScenario extends Simulation{
  var startTime: LocalDateTime = LocalDateTime.now()

  private val httpConf: HttpProtocolBuilder = http
    .baseUrl(Configurator.url)
  def userCount: Int = getProperty("USERS", Configurator.usersCount.toString).toInt
  def rampUpDuration: Int = getProperty("RAMP_DURATION", Configurator.rampUpDurationSeconds.toString).toInt
  def testDuration: Int = getProperty("TEST_DURATION", Configurator.testDurationSeconds.toString).toInt
  def user: String = getProperty("JENKINS_LOGIN", "POKA_NE_ADMIN")

  before {
    startTime = LocalDateTime.now()
    println(s"Test started at: $startTime by user: $user")
  }

  private val asserts = Seq(
    global.responseTime.percentile3.lte(5000)
  )
  private val orderCreation_OpenScenario: PopulationBuilder = {
    scenario("OrderCreation_Scenario_OpenModel")
      .during(testDuration)(OrderCreationChain.execute)
      .inject(ScenarioInjector.injectOpenModel(userCount, rampUpDuration))
      .protocols(httpConf)
      .andThen(
        SlackNotificator.sendNotificationAboutFinish(startTime, LocalDateTime.now().plusSeconds(testDuration))
          .inject(
            atOnceUsers(1)
          )
          .protocols(SlackNotificator.getSlackHTTPProtocol)
      )
  }
  setUp(
    //    SlackNotificator.sendNotificationAboutStart()
    //      .inject(atOnceUsers(1))
    //      .protocols(SlackNotificator.getSlackHTTPProtocol),
    orderCreation_OpenScenario
    //        .andThen(
    //          SlackNotificator.sendNotificationAboutFinish(startTime, LocalDateTime.now())
    //          .inject(atOnceUsers(1))
    //          .protocols(SlackNotificator.getSlackHTTPProtocol)
    //        )
  ).assertions(asserts)

  after {
    DbClient.buildInfoWriter(startTime)
  }

}
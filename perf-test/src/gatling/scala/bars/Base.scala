package bars

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import java.util.concurrent.TimeUnit

import io.gatling.core.Predef.{atOnceUsers, constantUsersPerSec, rampUsers}
import io.gatling.core.controller.inject.open.OpenInjectionStep
import io.gatling.core.scenario.Simulation
import org.testcontainers.containers.{Network, PostgreSQLContainer}

import scala.collection.immutable.ListSet
import scala.concurrent.duration.Duration
import scala.util.Random

abstract class Base extends Simulation {
  val NUM_AT_ONCE_USERS = 1000
  val NUM_STEADY_STATE_USERS = 100

  val RAMP_UP_DURATION = Duration.create(30, TimeUnit.SECONDS)
  val STEADY_STATE_DURATION = Duration.create(60, TimeUnit.SECONDS)

  val GET_REPEAT_TIMES = 10

  val injectionLoadType = LoadType.atOnce

  def name() = Random.alphanumeric.take(8).mkString


  object LoadType extends Enumeration {
    type LoadType = Value
    val atOnce, steadyLoad  = Value
  }

  def getLoad(): ListSet[OpenInjectionStep] ={
    if (injectionLoadType == LoadType.atOnce) {
      atOnceLoad()
    } else {
      steadyLoad()
    }
  }

  def steadyLoad(): ListSet[OpenInjectionStep] = ListSet(
    rampUsers(NUM_STEADY_STATE_USERS) during RAMP_UP_DURATION,
    constantUsersPerSec(NUM_STEADY_STATE_USERS) during STEADY_STATE_DURATION
  )

  def atOnceLoad(): ListSet[OpenInjectionStep] = ListSet(
    atOnceUsers(NUM_AT_ONCE_USERS)
  )

  val scn = scenario("Bars")
    .repeat(10)(exec(http("get").get("/bars").asJson))
    .exec(http("add").post("/bars").body(StringBody(s"""{"name": "${name()}"}""")).asJson)

  val network = Network.newNetwork()

  class PostgresContainer extends PostgreSQLContainer[PostgresContainer]("postgres:13.1")

  val postgresContainer = new PostgresContainer()
    .withInitScript("init.sql")
    .withNetwork(network)
    .withNetworkAliases("postgres")

  postgresContainer.start()

  after {
    postgresContainer.stop()
  }
}

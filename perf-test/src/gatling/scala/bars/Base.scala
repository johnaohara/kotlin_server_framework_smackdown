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

  val NUM_AT_ONCE_USERS: Int = if (System.getProperty("steadyStateUser") != null)  Integer.getInteger("steadyStateUser") else 1000
  val NUM_STEADY_STATE_USERS: Int = if (System.getProperty("steadyStateUser") != null)  Integer.getInteger("steadyStateUser") else 100

  val rampUpDuration: Int = if (System.getProperty("rampUpDuration") != null)  Integer.getInteger("rampUpDuration") else 30
  val steadyStateDuration: Int = if (System.getProperty("steadyStateDuration") != null)  Integer.getInteger("steadyStateDuration") else 60

  val RAMP_UP_DURATION  = Duration.create(rampUpDuration, TimeUnit.SECONDS)
  val STEADY_STATE_DURATION = Duration.create(steadyStateDuration, TimeUnit.SECONDS)

  val GET_REPEAT_TIMES = 10

  val injectionLoadType = LoadType.withName(if (System.getProperty("loadType") != null)  System.getProperty("loadType") else "atOnce" )

  def name() = Random.alphanumeric.take(8).mkString

  object LoadType extends Enumeration {
    type LoadType = Value
    val atOnce, steadyLoad  = Value
  }

  def getLoad(): ListSet[OpenInjectionStep] = injectionLoadType match {
    case LoadType.atOnce => atOnceLoad()
    case LoadType.steadyLoad => steadyLoad()
    case _ => throw new RuntimeException("Unknown Load type defined")
  }

  def steadyLoad(): ListSet[OpenInjectionStep] = ListSet(
    rampUsers(NUM_STEADY_STATE_USERS) during RAMP_UP_DURATION,
    constantUsersPerSec(NUM_STEADY_STATE_USERS) during STEADY_STATE_DURATION
  )

  def atOnceLoad(): ListSet[OpenInjectionStep] = ListSet(
    atOnceUsers(NUM_AT_ONCE_USERS)
  )

  val scn = scenario("Bars")
    .repeat(GET_REPEAT_TIMES)(exec(http("get").get("/bars").asJson))
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

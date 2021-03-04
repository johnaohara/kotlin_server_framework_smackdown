package bars

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

class QuarkusSimulation extends Base {

  class QuarkusContainer extends GenericContainer[QuarkusContainer]("quarkus-server")

  val quarkusContainer = new QuarkusContainer()
    .withExposedPorts(8080)
    .withNetwork(network)
    .waitingFor(Wait.forHttp("/bars"))
    .withEnv("QUARKUS_DATASOURCE_REACTIVE_URL", s"postgresql://postgres:5432/${postgresContainer.getDatabaseName}")
    .withEnv("QUARKUS_DATASOURCE_USERNAME", postgresContainer.getUsername)
    .withEnv("QUARKUS_DATASOURCE_PASSWORD", postgresContainer.getPassword)

  quarkusContainer.start()

  val httpProtocol = http.baseUrl(s"http://${quarkusContainer.getHost}:${quarkusContainer.getFirstMappedPort}")

  setUp(
    scn
      .inject {
        getLoad()
      }
      .protocols(httpProtocol)
  )

  after {
//    println(quarkusContainer.getLogs())
    quarkusContainer.stop()
  }

}
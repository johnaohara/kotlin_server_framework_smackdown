package bars

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

class SpringBootSimulation extends Base {

  class SpringBootContainer extends GenericContainer[SpringBootContainer]("springboot-server")

  val springbootContainer = new SpringBootContainer()
    .withExposedPorts(8080)
    .withNetwork(network)
    .waitingFor(Wait.forHttp("/bars"))
    .withEnv("SPRING_R2DBC_URL", s"r2dbc:postgresql://postgres/${postgresContainer.getDatabaseName}")
    .withEnv("SPRING_R2DBC_USERNAME", postgresContainer.getUsername)
    .withEnv("SPRING_R2DBC_PASSWORD", postgresContainer.getPassword)

  springbootContainer.start()

  val httpProtocol = http.baseUrl(s"http://${springbootContainer.getHost}:${springbootContainer.getFirstMappedPort}")

  setUp(
    scn
      .inject {
        getLoad()
      }
      .protocols(httpProtocol)
  )

  after {
    springbootContainer.stop()
  }

}
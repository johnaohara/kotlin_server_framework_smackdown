package bars

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

class KtorSimulation extends Base {

  class KtorContainer extends GenericContainer[KtorContainer]("ktor-server")

  val ktorContainer = new KtorContainer()
    .withExposedPorts(8080)
    .withNetwork(network)
    .waitingFor(Wait.forHttp("/bars"))
    .withEnv("JASYNC_CLIENT_HOST", "postgres")
    .withEnv("JASYNC_CLIENT_PORT", "5432")
    .withEnv("JASYNC_CLIENT_DATABASE", postgresContainer.getDatabaseName)
    .withEnv("JASYNC_CLIENT_USERNAME", postgresContainer.getUsername)
    .withEnv("JASYNC_CLIENT_PASSWORD", postgresContainer.getPassword)

  ktorContainer.start()

  val httpProtocol = http.baseUrl(s"http://${ktorContainer.getHost}:${ktorContainer.getFirstMappedPort}")

  setUp(
      scn
      .inject(
        steadyLoad()
      )
      .protocols(httpProtocol)
  )


  after {
    ktorContainer.stop()
  }

}
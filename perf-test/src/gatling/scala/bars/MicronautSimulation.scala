package bars

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

class MicronautSimulation extends Base {

  class MicronautContainer extends GenericContainer[MicronautContainer]("micronaut-server")

  val micronautContainer = new MicronautContainer()
    .withExposedPorts(8080)
    .withNetwork(network)
    .waitingFor(Wait.forHttp("/bars"))
    .withEnv("JASYNC_CLIENT_HOST", "postgres")
    .withEnv("JASYNC_CLIENT_PORT", "5432")
    .withEnv("JASYNC_CLIENT_DATABASE", postgresContainer.getDatabaseName)
    .withEnv("JASYNC_CLIENT_USERNAME", postgresContainer.getUsername)
    .withEnv("JASYNC_CLIENT_PASSWORD", postgresContainer.getPassword)

  micronautContainer.start()

  val httpProtocol = http.baseUrl(s"http://${micronautContainer.getHost}:${micronautContainer.getFirstMappedPort}")

  setUp(
      scn
      .inject(
        steadyLoad()
      )
      .protocols(httpProtocol)
  )

  after {
    micronautContainer.stop()
  }

}
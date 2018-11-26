package io.chrisdavenport.cls.sch.pipe

import cats.effect.IO
import cats.implicits._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.chrisdavenport.log4cats.Logger
import org.specs2.mutable.Specification
import io.chrisdavenport.testcontainersspecs2._
import com.dimafeng.testcontainers.GenericContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import java.time.Duration
import java.time.temporal.ChronoUnit.SECONDS

class MigrationSpec extends Specification with ForAllTestContainer {

  lazy val logger: Logger[IO] = Slf4jLogger.unsafeCreate[IO]

  // IMPORTANT: MUST BE LAZY VAL
  override lazy val container = GenericContainer(
    "postgres:10",
    List(5432),
    Map(
      "POSTGRES_DB" -> dbName,
      "POSTGRES_USER" -> dbUserName,
      "POSTGRES_PASSWORD" -> dbPassword,
      "POSTGRES_ROLE" -> dbName
    ),
    waitStrategy = new LogMessageWaitStrategy()
      .withRegEx(".*database system is ready to accept connections.*\\s")
      .withTimes(2)
      .withStartupTimeout(Duration.of(60, SECONDS))
  )
  lazy val dbUserName = "user"
  lazy val dbPassword = "password"
  lazy val dbName = "db"

  "Migrations" should {
    "makeMigrations works correctly" in {
      val host = container.container.getContainerIpAddress()
      val port = container.container.getMappedPort(5432)
      val pgConf  = config.PostgresConf(dbUserName, dbPassword, host, port.toString, dbName)
      config.makeMigrations[IO](pgConf)
        .attempt
          .flatMap{
            case Left(e) => logger.error(e)("Failed To Get Exected Valid Migration").as(Either.left(e))
            case Right(_) => IO(Right(()))
          }
        .map(_.isRight)
        .unsafeRunSync() must_===(true)
    }
  }


}
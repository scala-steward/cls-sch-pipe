package io.chrisdavenport.cls.sch.pipe

import doobie._
import cats.effect._
import cats.effect.IO
import doobie.specs2._
import io.chrisdavenport.testcontainersspecs2._
import io.chrisdavenport.fuuid._
import org.specs2.mutable.Specification
import com.dimafeng.testcontainers.GenericContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import java.time.Duration
import java.time.temporal.ChronoUnit.SECONDS

class IODaoSpec extends DaoSpec[IO] {
  implicit val CS = IO.contextShift(scala.concurrent.ExecutionContext.global)
  // Using this instead of IOAnalysisMatchers to avoid uninitialized field error
  override implicit val M: Effect[IO] = IO.ioConcurrentEffect
}

trait DaoSpec[F[_]] extends Specification with Checker[F] with ForAllTestContainer {
  sequential

  implicit val CS: ContextShift[F]

  // IMPORTANT: MUST BE LAZY VAL
  override lazy val container = GenericContainer(
    "postgres:10",
    List(5432),
    Map(
      "POSTGRES_DB" -> dbName,
      "POSTGRES_USER" -> dbUserName,
      "POSTGRES_PASSWORD" -> dbPassword
    ),
    waitStrategy = new LogMessageWaitStrategy()
      .withRegEx(".*database system is ready to accept connections.*\\s")
      .withTimes(2)
      .withStartupTimeout(Duration.of(60, SECONDS))
  )


  lazy val dbUserName = "user"
  lazy val dbPassword = "password"
  lazy val dbName = "db"
  lazy val dbHost = container.container.getContainerIpAddress()
  lazy val dbPort = container.container.getMappedPort(5432)
  lazy val dbConfig = config.PostgresConf(
    dbUserName,
    dbPassword,
    dbHost,
    dbPort,
    dbName,
    "org.postgresql.Driver",
    s"jdbc:postgresql://${dbHost}:${dbPort}/${dbName}"
  )

  lazy val transactor = Transactor.fromDriverManager[F](
    dbConfig.driver,
    dbConfig.jdbcUrl,
    dbConfig.username,
    dbConfig.password
  )

  override def afterStart : Unit =
    config.makeMigrations[IO](dbConfig).unsafeRunSync()

  check(dao.SwvptrmUrWebDao.select)
  check(dao.SwvptrmUrWebDao.insert)

  check(dao.SwvsubjWebDao.select)
  check(dao.SwvsubjWebDao.insert)

  check(dao.SwvareaPsptWebDao.select)
  check(dao.SwvareaPsptWebDao.insert)

  check(dao.SwvinstAsgnPtrmWebDao.select)
  check(dao.SwvinstAsgnPtrmWebDao.insert)

  check(dao.SwvspecSearchWebDao.select)
  check(dao.SwvspecSearchWebDao.insert)

  check(dao.SwvcampUrWebDao.select)
  check(dao.SwvcampUrWebDao.insert)

  check(dao.SwvsectWebDao.select)
  check(dao.SwvsectWebDao.insert)

}
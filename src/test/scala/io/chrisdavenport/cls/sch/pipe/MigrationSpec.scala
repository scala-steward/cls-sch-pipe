/*
 * Copyright (C) 2018  Christopher Davenport
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
      val pgConf  = config.PostgresConf(dbUserName, dbPassword, host, port, dbName,
        "org.postgresql.Driver",
        s"jdbc:postgresql://${host}:${port}/${dbName}"
      )
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
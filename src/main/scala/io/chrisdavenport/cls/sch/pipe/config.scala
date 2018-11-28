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

import io.chrisdavenport.log4cats._
import com.typesafe.config.ConfigFactory
import cats._
import cats.implicits._
import cats.effect._
import doobie._
import doobie.implicits._
import doobie.hikari.HikariTransactor 
import doobie.util.ExecutionContexts
import pureconfig._
import pureconfig.generic.auto._
import io.chrisdavenport.monoids._
import io.chrisdavenport.linebacker._
import io.chrisdavenport.system.effect.Console._
import io.chrisdavenport.system.effect.Environment._
import io.circe._
import io.circe.generic.semiauto._
import io.circe.yaml
import cats.derived._
import org.flywaydb.core.Flyway
import fs2.Stream
import fs2.io.file
import fs2.text
import java.nio.file._
import com.monovore.decline._

object config {

  final case class AppConf(
    oracle: OracleConf,
    postgres: PostgresConf,
  )

  final case class OracleConf(
    username: String,
    password: String,
    host: String,
    port: Int,
    sid: String,
    driver: String,
    jdbcUrl: String
  )

  final case class PostgresConf(
    username: String,
    password: String,
    host: String,
    port: Int,
    sid: String,
    driver: String,
    jdbcUrl: String
  )

  final case class OracleTransactor[F[_]](getTransactor: Transactor[F]) extends AnyVal
  final case class PostgresTransactor[F[_]](getTransactor: Transactor[F]) extends AnyVal

  def loadOracleTransactor[F[_]: Async: Logger: ContextShift](oc: OracleConf): Resource[F,OracleTransactor[F]] = for {
    _ <- Resource.liftF(Logger[F].info(s"Attempting to Connect to Oracle Database - '${oc.jdbcUrl}' as '${oc.username}'"))
    connectEC <- ExecutionContexts.fixedThreadPool[F](10)
    transactEC <- ExecutionContexts.cachedThreadPool[F]
    transactor <- HikariTransactor.newHikariTransactor[F](
      oc.driver,
      oc.jdbcUrl,
      oc.username,
      oc.password,
      connectEC,
      transactEC
    )
  } yield OracleTransactor(transactor)

  def loadPostgresTransactor[F[_]: Async: Logger: ContextShift](oc: PostgresConf): Resource[F,PostgresTransactor[F]] = for {
    _ <- Resource.liftF(Logger[F].info(s"Attempting to Connect to Postgres Database - '${oc.jdbcUrl}' as '${oc.username}'"))
    connectEC <- ExecutionContexts.fixedThreadPool[F](10)
    transactEC <- ExecutionContexts.cachedThreadPool[F]
    transactor <- HikariTransactor.newHikariTransactor[F](
      oc.driver,
      oc.jdbcUrl,
      oc.username,
      oc.password,
      connectEC,
      transactEC
    )
  } yield PostgresTransactor(transactor)

  def makeMigrations[F[_]: Sync](pc: PostgresConf): F[Unit] = Sync[F].delay{
    Flyway.configure()
      .dataSource(pc.jdbcUrl, pc.username, pc.password)
      .load()
      .migrate
  }
  final case class AppOptions(
    config: AppConfig,
    additionalConfigFile: Option[Path]
  )
  object AppOptions {
    val optsAppOptions = {
      val optionalConfigFile = Opts.option[Path]("file", short = "f", help = "Path to Additional Configuration File").orNone
      (AppConfig.optsAppConfig, optionalConfigFile).mapN(AppOptions.apply)
    }
  }

  final case class AppConfig(
    oracle: OracleConfig,
    postgres: PostgresConfig
  )
  object AppConfig {
    implicit val appConfigMonoid: Monoid[AppConfig] = semi.monoid
    implicit private def lastDecoder[A: Decoder]: Decoder[Last[A]] = 
      Decoder[Option[A]].map(Last(_))
    import io.circe.generic.auto._

    implicit val appConfigDecoder: Decoder[AppConfig] = deriveDecoder[AppConfig]

    val optsAppConfig: Opts[AppConfig] = {
      val oracleConfig: Opts[OracleConfig] = {
        val user = Opts.option[String]("oracle-user", help = "The oracle user to use").orNone.map(Last(_))
        val pass = Opts.option[String]("oracle-password", help = "The oracle password to use").orNone.map(Last(_))
        val host = Opts.option[String]("oracle-host", help = "The oracle host to connect to").orNone.map(Last(_))
        val port = Opts.option[Int]("oracle-port", help = "The oracle port to connect to").orNone.map(Last(_))
        val sid = Opts.option[String]("oracle-sid", help = "The oracle sid to connect to").orNone.map(Last(_))
        val driver = Opts.option[String]("oracle-driver", help = "The oracle driver to user, only necessary using something custom")
          .orNone.map(Last(_))
        val jdbcUrl = Opts.option[String]("oracle-jdbc-url", help = "The oracle jdbc url, only necessary to do something custom")
          .orNone.map(Last(_))
        (user,pass,host,port, sid, driver, jdbcUrl).mapN(OracleConfig.apply)
      }
      val postgresConfig: Opts[PostgresConfig] = {
        val user = Opts.option[String]("postgres-user", help = "The postgres user to use").orNone.map(Last(_))
        val pass = Opts.option[String]("postgres-password", help = "The postgres password to use").orNone.map(Last(_))
        val host = Opts.option[String]("postgres-host", help = "The postgres host to connect to").orNone.map(Last(_))
        val port = Opts.option[Int]("postgres-port", help = "The postgres port to connect to").orNone.map(Last(_))
        val sid = Opts.option[String]("postgres-sid", help = "The postgres sid to connect to").orNone.map(Last(_))
        val driver = Opts.option[String]("postgres-driver", help = "The postgres driver to user, only necessary using something custom")
          .orNone.map(Last(_))
        val jdbcUrl = Opts.option[String]("postgres-jdbc-url", help = "The postgres jdbc url, only necessary to do something custom")
          .orNone.map(Last(_))
        (user,pass,host,port, sid, driver, jdbcUrl).mapN(PostgresConfig.apply)
      }
      (oracleConfig, postgresConfig).mapN(AppConfig.apply)
    }
  }

  final case class PostgresConfig(
    username: Last[String],
    password: Last[String],
    host: Last[String],
    port: Last[Int],
    sid: Last[String],
    driver: Last[String],
    jdbcUrl: Last[String]
  )
  object PostgresConfig {
    implicit val pgConfigMonoid : Monoid[PostgresConfig] = semi.monoid
  }

  final case class OracleConfig(
    username: Last[String],
    password: Last[String],
    host: Last[String],
    port: Last[Int],
    sid: Last[String],
    driver: Last[String],
    jdbcUrl: Last[String]
  )
  object OracleConfig {
    implicit val ocConfigMonoid : Monoid[OracleConfig] = semi.monoid
  }

  // TOOD: Validation to return exactly which values are missing
  def appConf[F[_]: Sync](app: AppConfig): F[AppConf] = app match {
    case AppConfig(
      OracleConfig(
        Last(ousernameOpt),
        Last(opasswordOpt),
        Last(ohostOpt),
        Last(oportOpt),
        Last(osidOpt),
        Last(odriverOpt),
        Last(ojdbcUrlOpt)
      ),
      PostgresConfig(
        Last(pusernameOpt),
        Last(ppasswordOpt),
        Last(phostOpt),
        Last(pportOpt),
        Last(psidOpt),
        Last(pdriverOpt),
        Last(pjdbcUrlOpt)
      )
    ) => 
    val psid = psidOpt.getOrElse("")
    (
      ousernameOpt.toValidNel("Oracle Username")
    , opasswordOpt.toValidNel("Oracle Password")
    , ohostOpt.toValidNel("Oracle Host")
    , oportOpt.toValidNel("Oracle Port")
    , osidOpt.toValidNel("Oracle Sid")
    , odriverOpt.toValidNel("Oracle Driver")
    , pusernameOpt.toValidNel("Postgres Username")
    , ppasswordOpt.toValidNel("Postgres Password")
    , phostOpt.toValidNel("Postgres Host")
    , pportOpt.toValidNel("Postgres Port")
    , pdriverOpt.toValidNel("Postgres Driver")
    ).mapN{
      case (ousername, opassword, ohost, oport, osid, odriver
      , pusername, ppassword, phost, pport, pdriver) => 
      AppConf(
        OracleConf(ousername, opassword, ohost, oport, osid, odriver, 
        ojdbcUrlOpt.getOrElse(s"jdbc:oracle:thin:@//${ohost}:${oport}/${osid}")
      ),
      PostgresConf(pusername, ppassword, phost, pport, psid, pdriver,
        pjdbcUrlOpt.getOrElse(s"jdbc:postgresql://${phost}:${pport}/${psid}")
      )
      )
    }.fold(
      {s => 
        val missingConfigs = s.intercalate(", ")
        Sync[F].raiseError(
          new Throwable(
            show"""Missing one or more configuration options - Missing $missingConfigs
            |Please Check Configuration Options in the documentation or use --help""".stripMargin
          ) with scala.util.control.NoStackTrace
        )
      },
      _.pure[F]
    )
  }

  

  def loadAppConf[F[_]: Sync: DualContext : ContextShift: Logger](args: List[String]): F[AppConf] = for {
    cliConfig <- fromArgs[F](args)
    envAppConfig <- appConfigFromEnv[F]
    defaultFile <- getFromDefaultFile[F]
    additionalFile <- cliConfig.additionalConfigFile.traverse(getFromFile[F]).map(_.combineAll)
    finalConfig = defaultAppConfig |+| envAppConfig |+| defaultFile |+| additionalFile |+| cliConfig.config
    out <- appConf[F](finalConfig)
  } yield out

  val defaultAppConfig: AppConfig = AppConfig(
    OracleConfig(
      Last(None),
      Last(None),
      Last(None),
      Last(Some(2322)),
      Last(None),
      Last("oracle.jdbc.driver.OracleDriver".some),
      Last(None)
    ),
    PostgresConfig(
      Last(None), // username
      Last(None), // password
      Last(None), // host
      Last(Some(5432)), // port
      Last(None), // sid
      Last("org.postgresql.Driver".some), // driver
      Last(None) // jdbcUrl
    )
  )

  def appConfigFromEnv[F[_]: Sync]: F[AppConfig] = for {
    ousername <- lookupEnv[F]("ORACLE_USER")
    opassword <- lookupEnv[F]("ORACLE_PASS")
    ohost     <- lookupEnv[F]("ORACLE_HOST")
    oport     <- lookupEnv[F]("ORACLE_PORT").flatMap(i => Sync[F].delay(i.map(_.toInt)))
    osid      <- lookupEnv[F]("ORACLE_SID")
    odriver   <- lookupEnv[F]("ORACLE_DRIVER")
    ojdbcUrl  <- lookupEnv[F]("ORACLE_JDBC_URL")
    pusername <- lookupEnv[F]("POSTGRES_USER")
    ppassword <- lookupEnv[F]("POSTGRES_PASS")
    phost     <- lookupEnv[F]("POSTGRES_HOST")
    pport     <- lookupEnv[F]("POSTGRES_PORT").flatMap(i => Sync[F].delay(i.map(_.toInt)))
    psid      <- lookupEnv[F]("POSTGRES_SID")
    pdriver   <- lookupEnv[F]("POSTGRES_DRIVER")
    pjdbcUrl  <- lookupEnv[F]("POSTGRES_JDBC_URL")
  } yield AppConfig(
    OracleConfig(
      Last(ousername),
      Last(opassword),
      Last(ohost),
      Last(oport),
      Last(osid),
      Last(odriver),
      Last(ojdbcUrl)
    ),
    PostgresConfig(
      Last(pusername),
      Last(ppassword),
      Last(phost),
      Last(pport),
      Last(psid),
      Last(pdriver),
      Last(pjdbcUrl)
    )
  )

  def getFromFile[F[_]: Sync : DualContext: ContextShift: Logger](path: Path): F[AppConfig] = 
    Stream.eval(Sync[F].delay(Files.exists(path)))
    .ifM(
      path.pure[Stream[F, ?]], 
      Stream.eval(Sync[F].raiseError(new Exception("File Does Not Exist") with scala.util.control.NoStackTrace))
    ).flatMap(
      file.readAll[F](_, DualContext[F].blockingContext, 512)
    ).through(text.utf8Decode)
    .compile
    .foldMonoid
    .flatMap(yaml.parser.parse(_).liftTo[F])
    .flatMap(_.as[AppConfig].liftTo[F])
    .recoverWith{
      case e => // Recover to An Empty Config
      Logger[F].warn(e)(s"Failed to Load Config File - $path")
        .as(Monoid[AppConfig].empty)
    }

  def getFromDefaultFile[F[_]: Sync: DualContext: ContextShift: Logger]: F[AppConfig] = 
    getFromFile[F](FileSystems.getDefault().getPath("/usr", "local", "etc", "cls-sch-pipe.yml"))

  def fromArgs[F[_]: Sync](args: List[String]): F[AppOptions] = {
    Command(
      name = "cls-sch-pipe",
      header = "Run Database pipe."
    )(AppOptions.optsAppOptions)
    .parse(args)
    .fold(
      h => putStrLn[F](h.toString) >> 
        Sync[F].raiseError[AppOptions](new Throwable("Command Line Options Invalid") with scala.util.control.NoStackTrace),
      _.pure[F]
    )
  }


}
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
import cats.derived._
import org.flywaydb.core.Flyway

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
    sid: String
  ){
    val driver = "oracle.jdbc.driver.OracleDriver"
    def jdbcUrl: String = s"jdbc:oracle:thin:@//${host}:${port}/${sid}"
  }

  final case class PostgresConf(
    username: String,
    password: String,
    host: String,
    port: String,
    sid: String
  ){
    val driver = "org.postgresql.Driver"
    def jdbcUrl: String = s"jdbc:postgresql://${host}:${port}/${sid}"
  }

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

  final case class AppConfig(
    oracle: OracleConfig,
    postgres: PostgresConfig
  )
  object AppConfig {
    implicit val appConfigSemigroup: Semigroup[AppConfig] = semi.semigroup
  }

  final case class PostgresConfig(
    username: Last[String],
    password: Last[String],
    host: Last[String],
    port: Last[String],
    sid: Last[String]
  )
  object PostgresConfig {
    implicit val pgConfigSemigroup : Semigroup[PostgresConfig] = semi.semigroup
  }

  final case class OracleConfig(
    username: Last[String],
    password: Last[String],
    host: Last[String],
    port: Last[Int],
    sid: Last[String]
  )
  object OracleConfig {
    implicit val ocConfigSemigroup : Semigroup[OracleConfig] = semi.semigroup
  }

  private def loadAppConfig[F[_]: Sync]: F[AppConfig] = for {
    classLoader <- Sync[F].delay(ConfigFactory.load(getClass().getClassLoader()))
    out <- Sync[F].delay(loadConfigOrThrow[AppConfig](classLoader, ""))
  } yield out

  private def appConf[F[_]: Sync](app: AppConfig): F[AppConf] = app match {
    case AppConfig(
      OracleConfig(
        Last(Some(ousername)),
        Last(Some(opassword)),
        Last(Some(ohost)),
        Last(Some(oport)),
        Last(Some(osid))
      ),
      PostgresConfig(
        Last(Some(pusername)),
        Last(Some(ppassword)),
        Last(Some(phost)),
        Last(Some(pport)),
        Last(sidOpt)
      )
    ) => AppConf(
      OracleConf(ousername, opassword, ohost, oport, osid),
      PostgresConf(pusername, ppassword, phost, pport, sidOpt.getOrElse(""))
    ).pure[F]
    case o => Sync[F].raiseError(new Throwable(s"Missing one or more configuration options - Got $o"))
  }

  def loadAppConf[F[_]: Sync]: F[AppConf] = loadAppConfig[F] >>= appConf[F]

}
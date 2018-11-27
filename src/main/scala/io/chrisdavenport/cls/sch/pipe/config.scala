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

  final case class AppConfig(
    oracle: OracleConfig,
    postgres: PostgresConfig
  )
  object AppConfig {
    implicit val appConfigSemigroup: Semigroup[AppConfig] = semi.semigroup
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
    implicit val pgConfigSemigroup : Semigroup[PostgresConfig] = semi.semigroup
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
    implicit val ocConfigSemigroup : Semigroup[OracleConfig] = semi.semigroup
  }

  // TOOD: Validation to return exactly which values are missing
  def appConf[F[_]: Sync](app: AppConfig): F[AppConf] = app match {
    case AppConfig(
      OracleConfig(
        Last(Some(ousername)),
        Last(Some(opassword)),
        Last(Some(ohost)),
        Last(Some(oport)),
        Last(Some(osid)),
        Last(Some(odriver)),
        Last(ojdbcUrl)
      ),
      PostgresConfig(
        Last(Some(pusername)),
        Last(Some(ppassword)),
        Last(Some(phost)),
        Last(Some(pport)),
        Last(sidOpt),
        Last(Some(pdriver)),
        Last(pjdbcUrl)
      )
    ) => 
    val psid = sidOpt.getOrElse("")
    AppConf(
      OracleConf(ousername, opassword, ohost, oport, osid, odriver, 
        ojdbcUrl.getOrElse(s"jdbc:oracle:thin:@//${ohost}:${oport}/${osid}")
      ),
      PostgresConf(pusername, ppassword, phost, pport, psid, pdriver,
        pjdbcUrl.getOrElse(s"jdbc:postgresql://${phost}:${pport}/${psid}")
      )
    ).pure[F]
    case o => Sync[F].raiseError(new Throwable(s"Missing one or more configuration options - Got $o"))
  }

  

  def loadAppConf[F[_]: Sync: DualContext : ContextShift: Logger](args: List[String]): F[AppConf] = for {
    cliConfig <- fromArgs[F](args)
    envAppConfig <- appConfigFromEnv[F]
    defaultFile <- getFromDefaultFile[F]
    finalConfig = defaultAppConfig.combine(envAppConfig).combine(defaultFile).combine(cliConfig)
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
      Logger[F].warn(e)(s"Failed to Load Config File - $path").as(
          AppConfig(
            OracleConfig(Last(None), Last(None), Last(None), Last(None), Last(None), Last(None), Last(None)),
            PostgresConfig(Last(None), Last(None), Last(None), Last(None), Last(None), Last(None), Last(None)),
          )
        )
    }

  def getFromDefaultFile[F[_]: Sync: DualContext: ContextShift: Logger]: F[AppConfig] = 
    getFromFile[F](FileSystems.getDefault().getPath("/usr", "local", "etc", "cls-sch-pipe.yml"))

  def fromArgs[F[_]: Sync](args: List[String]): F[AppConfig] = {
    Command(
      name = "cls-sch-pipe",
      header = "Run Database pipe."
    ) {AppConfig.optsAppConfig}
    .parse(args)
    .fold(
      h => putStrLn[F](h.toString) >> Sync[F].raiseError[AppConfig](new Throwable("Command Line Options Invalid") with scala.util.control.NoStackTrace),
      _.pure[F]
    )
  }


}
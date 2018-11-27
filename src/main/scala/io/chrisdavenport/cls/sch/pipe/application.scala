package io.chrisdavenport.cls.sch.pipe

import cats._
import cats.implicits._
import cats.effect._
import doobie._
import doobie.implicits._
import io.chrisdavenport.linebacker._
import io.chrisdavenport.log4cats._
import io.chrisdavenport.log4cats.slf4j._
import io.chrisdavenport.system.effect.Console._
import io.chrisdavenport.cls.sch.pipe.oracle._
import io.chrisdavenport.cls.sch.pipe.postgres._
import fs2._
import scala.concurrent.duration._

object application {

  def run[F[_]: ConcurrentEffect: Timer: ContextShift](args: List[String]): Resource[F, ExitCode] = for {
    implicit0(logger: Logger[F]) <- Resource.liftF(Slf4jLogger.create[F])
    implicit0(dc: DualContext[F]) <- contexts.Executors.unbound[F]
      .map(DualContext.fromExecutorService[F](implicitly[ContextShift[F]], _))
    conf <- Resource.liftF(config.loadAppConf[F](args))
    _ <- Resource.liftF(migration[F](conf))
    out <- Resource.liftF(stream(conf).drain.compile.drain)
  } yield ExitCode.Success


  def migration[F[_]: Logger: ConcurrentEffect: Timer: ContextShift](conf: config.AppConf): F[Unit] = {
    config.makeMigrations[F](conf.postgres)
  }

  def stream[F[_]: Logger: ConcurrentEffect: Timer: ContextShift](conf: config.AppConf): Stream[F, Unit] = {
    ( 
      application.singleCycle(conf) ++ 
      Stream.awakeDelay[F](24.hours) >> application.singleCycle(conf)
    )
      .repeat
      .handleErrorWith(e => 
        Stream.eval(Logger[F].error(e)("Unexpected Error In Stream Cycle")) ++
        Stream.sleep_(1.minute).covary[F].drain ++
        stream[F](conf)
      )
  }

  def singleCycle[F[_]: Logger: ConcurrentEffect: Timer: ContextShift](conf: config.AppConf): Stream[F, Unit] = for {
    oracle <- Stream.resource(config.loadOracleTransactor[F](conf.oracle))
      .map(Oracle.impl[F])
    postgres <- Stream.resource(config.loadPostgresTransactor[F](conf.postgres))
      .map(Postgres.impl[F])

    _ <- Stream(
      Stream.eval(oracle.getSwvptrmUrWeb)
        .evalTap(l => Logger[F].info(show"Oracle SwvptrmUrWeb - Returned ${l.length}"))
        .evalMap(postgres.writeSwvptrmUrWeb)
        .evalTap(i => Logger[F].info(show"Postgres Swvptrm - Wrote $i"))
      ,
      Stream.eval(oracle.getSwvsubjWeb)
        .evalTap(l => Logger[F].info(show"Oracle SwvsubjWeb - Returned ${l.length}"))
        .evalMap(postgres.writeSwvsubjWeb)
        .evalTap(i => Logger[F].info(show"Postgres SwvsubjWeb - Wrote $i"))
      ,
      Stream.eval(oracle.getSwvareaPsptWeb)
        .evalTap(l => Logger[F].info(show"Oracle SwvareaPsptWeb - Returned ${l.length}"))
        .evalMap(postgres.writeSwvareaPsptWeb)
        .evalTap(i => Logger[F].info(show"Postgres SwvareaPsptWeb - Wrote $i"))
      ,
      Stream.eval(oracle.getSwvinstAsgnPtrmWeb)
        .evalTap(l => Logger[F].info(show"Oracle SwvinstAsgnPtrmWeb - Returned ${l.length}"))
        .evalMap(postgres.writeSwvinstAsgnPtrmWeb)
        .evalTap(i => Logger[F].info(show"Postgres SwvinstAsgnPtrmWeb - Wrote $i"))
      ,
      Stream.eval(oracle.getSwvspecSearchWeb)
        .evalTap(l => Logger[F].info(show"Oracle SwvspecSearchWeb - Returned ${l.length}"))
        .evalMap(postgres.writeSwvspecSearchWeb)
        .evalTap(i => Logger[F].info(show"Postgres SwvspecSearchWeb - Wrote $i"))
      ,
      Stream.eval(oracle.getSwvcampUrWeb)
        .evalTap(l => Logger[F].info(show"Oracle SwvcampUrWeb - Returned ${l.length}"))
        .evalMap(postgres.writeSwvcampUrWeb)
        .evalTap(i => Logger[F].info(show"Postgres SwvcampUrWeb - Wrote $i"))
      ,
      Stream.eval(oracle.getSwvsectWeb)
        .evalTap(l => Logger[F].info(show"Oracle SwvsectWeb - Returned ${l.length}"))
        .evalMap(postgres.writeSwvsectWeb)
        .evalTap(i => Logger[F].info(show"Postgres SwvsectWeb - Wrote $i"))
    ).covary[F]
      .parJoinUnbounded
      .drain ++ Stream.eval(Logger[F].info("Cycle Completed"))
  } yield ()
}
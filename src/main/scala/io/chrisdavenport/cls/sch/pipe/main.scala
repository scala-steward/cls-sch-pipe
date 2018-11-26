package io.chrisdavenport.cls.sch.pipe

import cats.implicits._
import cats.effect._
import io.chrisdavenport.log4cats._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import fs2._
import scala.concurrent.duration._

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = for {
    implicit0(logger: Logger[IO]) <- Slf4jLogger.create[IO]
    conf <- config.loadAppConf[IO]
    _ <- application.migration[IO](conf)
    out <- application.stream(conf).drain.compile.drain
  } yield ExitCode.Success

}
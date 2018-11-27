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

import cats.implicits._
import cats.effect._
import io.chrisdavenport.log4cats._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import fs2._
import scala.concurrent.duration._

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = for {
    out <- application.run[IO](args).use(_.pure[IO])
  } yield out

}
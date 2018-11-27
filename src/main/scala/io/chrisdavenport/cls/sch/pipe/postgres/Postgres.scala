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

package io.chrisdavenport.cls.sch.pipe.postgres

import cats.effect._
import doobie.implicits._
import io.chrisdavenport.cls.sch.pipe.config.PostgresTransactor
import io.chrisdavenport.cls.sch.pipe.dao._
import io.chrisdavenport.cls.sch.pipe.models._

trait Postgres[F[_]]{
  def getSwvptrmUrWeb: F[List[SwvptrmUrWeb]]
  def writeSwvptrmUrWeb(l: List[SwvptrmUrWeb]): F[Int]

  def getSwvsubjWeb: F[List[SwvsubjWeb]]
  def writeSwvsubjWeb(l: List[SwvsubjWeb]): F[Int]

  def getSwvareaPsptWeb: F[List[SwvareaPsptWeb]]
  def writeSwvareaPsptWeb(l: List[SwvareaPsptWeb]): F[Int]

  def getSwvinstAsgnPtrmWeb: F[List[SwvinstAsgnPtrmWeb]]
  def writeSwvinstAsgnPtrmWeb(l: List[SwvinstAsgnPtrmWeb]): F[Int]

  def getSwvspecSearchWeb: F[List[SwvspecSearchWeb]]
  def writeSwvspecSearchWeb(l: List[SwvspecSearchWeb]): F[Int]

  def getSwvcampUrWeb: F[List[SwvcampUrWeb]]
  def writeSwvcampUrWeb(l: List[SwvcampUrWeb]): F[Int]

  def getSwvsectWeb: F[List[SwvsectWeb]]
  def writeSwvsectWeb(l: List[SwvsectWeb]): F[Int]

}

object Postgres {
  def impl[F[_]: Sync](oc: PostgresTransactor[F]): Postgres[F] = new Postgres[F]{
    private val transactor = oc.getTransactor
    def getSwvptrmUrWeb: F[List[SwvptrmUrWeb]] =
      SwvptrmUrWebDao.selectAll.transact(transactor)
    def writeSwvptrmUrWeb(l: List[SwvptrmUrWeb]): F[Int] =
      SwvptrmUrWebDao.insertMany(l).transact(transactor)

    def getSwvsubjWeb: F[List[SwvsubjWeb]] =
      SwvsubjWebDao.selectAll.transact(transactor)
    def writeSwvsubjWeb(l: List[SwvsubjWeb]): F[Int] = 
      SwvsubjWebDao.insertMany(l).transact(transactor)

    def getSwvareaPsptWeb: F[List[SwvareaPsptWeb]] = 
      SwvareaPsptWebDao.selectAll.transact(transactor)
    def writeSwvareaPsptWeb(l: List[SwvareaPsptWeb]): F[Int] =
      SwvareaPsptWebDao.insertMany(l).transact(transactor)

    def getSwvinstAsgnPtrmWeb: F[List[SwvinstAsgnPtrmWeb]] = 
      SwvinstAsgnPtrmWebDao.selectAll.transact(transactor)
    def writeSwvinstAsgnPtrmWeb(l: List[SwvinstAsgnPtrmWeb]): F[Int] = 
      SwvinstAsgnPtrmWebDao.insertMany(l).transact(transactor)

    def getSwvspecSearchWeb: F[List[SwvspecSearchWeb]] =
      SwvspecSearchWebDao.selectAll.transact(transactor)
    def writeSwvspecSearchWeb(l: List[SwvspecSearchWeb]): F[Int] = 
      SwvspecSearchWebDao.insertMany(l).transact(transactor)

    def getSwvcampUrWeb: F[List[SwvcampUrWeb]] = 
      SwvcampUrWebDao.selectAll.transact(transactor)
    def writeSwvcampUrWeb(l: List[SwvcampUrWeb]): F[Int] =
      SwvcampUrWebDao.insertMany(l).transact(transactor)

    def getSwvsectWeb: F[List[SwvsectWeb]] =
      SwvsectWebDao.selectAll.transact(transactor)
    def writeSwvsectWeb(l: List[SwvsectWeb]): F[Int] =
      SwvsectWebDao.insertMany(l).transact(transactor)
  }

}
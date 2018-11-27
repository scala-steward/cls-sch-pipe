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

package io.chrisdavenport.cls.sch.pipe.oracle

import cats.effect._
import doobie.implicits._
import io.chrisdavenport.cls.sch.pipe.config.OracleTransactor
import io.chrisdavenport.cls.sch.pipe.dao._
import io.chrisdavenport.cls.sch.pipe.models._

trait Oracle[F[_]]{
  def getSwvptrmUrWeb: F[List[SwvptrmUrWeb]]
  def getSwvsubjWeb: F[List[SwvsubjWeb]]
  def getSwvareaPsptWeb: F[List[SwvareaPsptWeb]]
  def getSwvinstAsgnPtrmWeb: F[List[SwvinstAsgnPtrmWeb]]
  def getSwvspecSearchWeb: F[List[SwvspecSearchWeb]]
  def getSwvcampUrWeb: F[List[SwvcampUrWeb]]
  def getSwvsectWeb: F[List[SwvsectWeb]]
}

object Oracle {
  def impl[F[_]: Sync](oc: OracleTransactor[F]): Oracle[F] = new Oracle[F]{
    private val transactor = oc.getTransactor
    def getSwvptrmUrWeb: F[List[SwvptrmUrWeb]] =
      SwvptrmUrWebDao.selectAll.transact(transactor)
    def getSwvsubjWeb: F[List[SwvsubjWeb]] =
      SwvsubjWebDao.selectAll.transact(transactor)
    def getSwvareaPsptWeb: F[List[SwvareaPsptWeb]] = 
      SwvareaPsptWebDao.selectAll.transact(transactor)
    def getSwvinstAsgnPtrmWeb: F[List[SwvinstAsgnPtrmWeb]] = 
      SwvinstAsgnPtrmWebDao.selectAll.transact(transactor)
    def getSwvspecSearchWeb: F[List[SwvspecSearchWeb]] =
      SwvspecSearchWebDao.selectAll.transact(transactor)
    def getSwvcampUrWeb: F[List[SwvcampUrWeb]] = 
      SwvcampUrWebDao.selectAll.transact(transactor)
    def getSwvsectWeb: F[List[SwvsectWeb]] =
      SwvsectWebDao.selectAll.transact(transactor)
  }

}
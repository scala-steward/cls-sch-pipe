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
package io.chrisdavenport.cls.sch.pipe

import cats.implicits._
import doobie._
import doobie.implicits._
import models._

object dao {

  object SwvptrmUrWebDao {
    val select: Query0[SwvptrmUrWeb] = 
      sql"SELECT TERM, PTRM, PTRM_DESC, PTRM_START, PTRM_END FROM SWVPTRM_UR_WEB"
        .query[SwvptrmUrWeb]

    val insert: Update[SwvptrmUrWeb] = {
      val s = """INSERT INTO SWVPTRM_UR_WEB (TERM, PTRM, PTRM_DESC, PTRM_START, PTRM_END) 
      |VALUES (?, ?, ?, ?, ?)
      |ON CONFLICT (TERM, PTRM) DO UPDATE SET
      |(PTRM_DESC, PTRM_START, PTRM_END) = 
      |(EXCLUDED.PTRM_DESC, EXCLUDED.PTRM_START, EXCLUDED.PTRM_END)""".stripMargin
      Update[SwvptrmUrWeb](s)
    }

    val insertMany: List[SwvptrmUrWeb] => ConnectionIO[Int] = insert.updateMany(_)
    val selectAll: ConnectionIO[List[SwvptrmUrWeb]] = select.to[List]
  }

  object SwvsubjWebDao {
    val select: Query0[SwvsubjWeb] = 
      sql"SELECT TERM, SUBJ, SUBJ_DESC, PTRM FROM SWVSUBJ_WEB".query[SwvsubjWeb]
    val insert: Update[SwvsubjWeb] = {
      val s = """INSERT INTO SWVSUBJ_WEB (TERM, SUBJ, SUBJ_DESC, PTRM)
      |VALUES (?, ?, ?,?)
      |ON CONFLICT (TERM, PTRM, SUBJ) DO UPDATE SET
      |SUBJ_DESC = EXCLUDED.SUBJ_DESC""".stripMargin
      Update[SwvsubjWeb](s)
    }

    val selectAll: ConnectionIO[List[SwvsubjWeb]] = select.to[List]
    val insertMany: List[SwvsubjWeb] => ConnectionIO[Int] = insert.updateMany(_)
  }

  object SwvareaPsptWebDao {
    val select: Query0[SwvareaPsptWeb] =
      sql"SELECT AREA, AREA_DESC FROM SWVAREA_PSPT_WEB".query[SwvareaPsptWeb]
    val insert: Update[SwvareaPsptWeb] = {
      val s = """INSERT INTO SWVAREA_PSPT_WEB (AREA, AREA_DESC)
      |VALUES (?, ?)
      |ON CONFLICT (AREA) DO UPDATE SET
      |AREA_DESC = EXCLUDED.AREA_DESC
      """.stripMargin
      Update[SwvareaPsptWeb](s)
    }

    val insertMany: List[SwvareaPsptWeb] => ConnectionIO[Int] = insert.updateMany(_)
    val selectAll: ConnectionIO[List[SwvareaPsptWeb]] = select.to[List]
  }

  object SwvinstAsgnPtrmWebDao {
    val select : Query0[SwvinstAsgnPtrmWeb] =
      sql"SELECT TERM, PTRM, PREF_NAME, CA_EMAIL, PREF_FIRST_NAME, PIDM FROM SWVINST_ASGN_PTRM_WEB"
        .query[SwvinstAsgnPtrmWeb]
    val insert : Update[SwvinstAsgnPtrmWeb] = {
      val s = """INSERT INTO SWVINST_ASGN_PTRM_WEB (TERM, PTRM, PREF_NAME, CA_EMAIL, PREF_FIRST_NAME, PIDM)
      | VALUES (?, ?, ?, ?,?, ?)
      | ON CONFLICT (TERM, PTRM, PIDM) DO UPDATE SET
      | PREF_NAME = EXCLUDED.PREF_NAME,
      | CA_EMAIL = EXCLUDED.CA_EMAIL,
      | PREF_FIRST_NAME = EXCLUDED.PREF_FIRST_NAME""".stripMargin
      Update[SwvinstAsgnPtrmWeb](s)
    }

    val selectAll : ConnectionIO[List[SwvinstAsgnPtrmWeb]] = select.to[List]
    val insertMany: List[SwvinstAsgnPtrmWeb] => ConnectionIO[Int] = insert.updateMany(_)
  }

  object SwvspecSearchWebDao {
    val select: Query0[SwvspecSearchWeb] = 
      sql"SELECT ATTR, ATTR_DESC FROM SWVSPEC_SEARCH_WEB"
        .query[SwvspecSearchWeb]
    val insert: Update[SwvspecSearchWeb] = {
      val s = """INSERT INTO SWVSPEC_SEARCH_WEB (ATTR, ATTR_DESC)
      |VALUES (?, ?)
      |ON CONFLICT (ATTR) DO UPDATE SET
      |ATTR_DESC = EXCLUDED.ATTR_DESC""".stripMargin
      Update[SwvspecSearchWeb](s)
    }

    val selectAll : ConnectionIO[List[SwvspecSearchWeb]] = select.to[List]
    val insertMany: List[SwvspecSearchWeb] => ConnectionIO[Int] = insert.updateMany(_)
  }

  object SwvcampUrWebDao {
    val select : Query0[SwvcampUrWeb] =
      sql"SELECT TERM, PTRM, CAMP, CAMP_DESC FROM SWVCAMP_UR_WEB"
        .query[SwvcampUrWeb]
    val insert: Update[SwvcampUrWeb] = {
      val s = """INSERT INTO SWVCAMP_UR_WEB (TERM, PTRM, CAMP, CAMP_DESC)
      |VALUES (?,?,?,?)
      |ON CONFLICT (TERM, PTRM, CAMP) DO UPDATE SET
      |CAMP_DESC = EXCLUDED.CAMP_DESC
      """.stripMargin
      Update[SwvcampUrWeb](s)
    }

    val selectAll: ConnectionIO[List[SwvcampUrWeb]] = select.to[List]
    val insertMany: List[SwvcampUrWeb] => ConnectionIO[Int] = insert.updateMany(_)
  }

  object SwvsectWebDao {
    val select: Query0[SwvsectWeb] =
      sql"""SELECT
      CRN,
      SUBJ,
      SUBJ_SEARCH,
      SUBJ_DESC,
      CRSE_NUMB,
      SEQ_NUMB,
      COURSE,
      CRSE_TITLE,
      COURSE_COLL,
      COURSE_COLL_DESC,

      TERM,
      TERM_DESC,
      PTRM,
      SSTS,
      CAMP,
      DAYS,
      MEET_SCHD,
      MEET_TIME,
      INSTRUCT,
      INSTRUCT_ALL,
      INSTRUCT_PRIM,

      PREREQ,
      COREQ,

      DATES,
      LOCATION,
      SESS,
      SPECIAL,
      SPECIAL_DESC,
      TEXT,

      BLDG,
      ROOM,
      BN_TERM,
      CRSE_TEXT,
      
      CAPACITY,
      ENRL,
      REMAIN,
      CAPACITY_XLST,
      ENRL_XLST,
      REMAIN_XLST,
      BILL_HRS

      FROM SWVSECT_WEB""".query[SwvsectWeb]

    val insert : Update[SwvsectWeb] = {
      val s = """INSERT INTO SWVSECT_WEB (
        |CRN,
        |SUBJ,
        |SUBJ_SEARCH,
        |SUBJ_DESC,
        |CRSE_NUMB,
        |SEQ_NUMB,
        |COURSE,
        |CRSE_TITLE,
        |COURSE_COLL,
        |COURSE_COLL_DESC,
        |
        |TERM,
        |TERM_DESC,
        |PTRM,
        |SSTS,
        |CAMP,
        |DAYS,
        |MEET_SCHD,
        |MEET_TIME,
        |INSTRUCT,
        |INSTRUCT_ALL,
        |INSTRUCT_PRIM,
        |
        |PREREQ,
        |COREQ,
        |
        |DATES,
        |LOCATION,
        |SESS,
        |SPECIAL,
        |SPECIAL_DESC,
        |TEXT,
        |
        |BLDG,
        |ROOM,
        |BN_TERM,
        |CRSE_TEXT,
        |
        |CAPACITY,
        |ENRL,
        |REMAIN,
        |CAPACITY_XLST,
        |ENRL_XLST,
        |REMAIN_XLST,
        |BILL_HRS
        |)
        |VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        |ON CONFLICT (TERM, PTRM, SUBJ, CRSE_NUMB, SEQ_NUMB) DO UPDATE SET
        |CRN = EXCLUDED.CRN,
        |SUBJ_SEARCH = EXCLUDED.SUBJ_SEARCH,
        |SUBJ_DESC = EXCLUDED.SUBJ_DESC,
        |COURSE = EXCLUDED.COURSE,
        |CRSE_TITLE = EXCLUDED.CRSE_TITLE,
        |COURSE_COLL = EXCLUDED.COURSE_COLL,
        |COURSE_COLL_DESC = EXCLUDED.COURSE_COLL_DESC,
        |TERM_DESC = EXCLUDED.TERM_DESC,
        |SSTS = EXCLUDED.SSTS,
        |CAMP = EXCLUDED.CAMP,
        |DAYS = EXCLUDED.DAYS,
        |MEET_SCHD = EXCLUDED.MEET_SCHD,
        |MEET_TIME = EXCLUDED.MEET_TIME,
        |INSTRUCT = EXCLUDED.INSTRUCT,
        |INSTRUCT_ALL = EXCLUDED.INSTRUCT_ALL,
        |INSTRUCT_PRIM = EXCLUDED.INSTRUCT_PRIM,
        |PREREQ = EXCLUDED.PREREQ,
        |COREQ = EXCLUDED.COREQ,
        |DATES = EXCLUDED.DATES,
        |LOCATION = EXCLUDED.LOCATION,
        |SESS = EXCLUDED.SESS,
        |SPECIAL = EXCLUDED.SPECIAL,
        |SPECIAL_DESC = EXCLUDED.SPECIAL_DESC,
        |TEXT = EXCLUDED.TEXT,
        |BLDG = EXCLUDED.BLDG,
        |ROOM = EXCLUDED.ROOM,
        |BN_TERM = EXCLUDED.BN_TERM,
        |CRSE_TEXT = EXCLUDED.CRSE_TEXT,
        |CAPACITY = EXCLUDED.CAPACITY,
        |ENRL = EXCLUDED.ENRL,
        |REMAIN = EXCLUDED.REMAIN,
        |CAPACITY_XLST = EXCLUDED.CAPACITY_XLST,
        |ENRL_XLST = EXCLUDED.ENRL_XLST,
        |REMAIN_XLST = EXCLUDED.REMAIN_XLST,
        |BILL_HRS = EXCLUDED.BILL_HRS
        |""".stripMargin
      Update[SwvsectWeb](s)
    }

    val selectAll: ConnectionIO[List[SwvsectWeb]] = select.to[List]
    val insertMany: List[SwvsectWeb] => ConnectionIO[Int] = insert.updateMany
  }

}
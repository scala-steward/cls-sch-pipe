package io.chrisdavenport.cls.sch.pipe

import java.time._
import cats._

object models {
  final case class SwvptrmUrWeb(
    term: String,
    ptrm: String,
    ptrmDesc: String,
    ptrmStart: Instant,
    ptrmEnd: Instant
  )

  final case class SwvsubjWeb(
    term: String,
    subj: String,
    subjDesc: String,
    ptrm: String
  )

  final case class SwvareaPsptWeb(
    area: String,
    areaDesc: String
  )

  final case class SwvinstAsgnPtrmWeb(
    term: String,
    pterm: String,
    prefName: String,
    caEmail: Option[String],
    prefFirstName: String,
    pidm: Int
  )

  final case class SwvspecSearchWeb(
    attr: String,
    attrDesc: String
  )

  final case class SwvcampUrWeb(
    term: String,
    ptrm: String,
    camp: String,
    campDesc: String
  )

  final case class CourseInfo(
    crn: String,
    subj: String,
    subjSearch: String,
    subjDesc: String,
    crseNumb: String,
    seqNumb: String,
    course: String,
    courseTitle: String,
    courseColl: String,
    courseCollDesc: String
  )
  final case class TermInfo(
    term: String,
    termDesc: String,
    ptrm: String,
    ssts: String,
    camp: String,
    days: String,
    meet_schd: String,
    meet_time: String,
    instruct: String,
    instructAll: String,
    instructPrim: String
  )
  final case class CapacityInfo(
    capacity: Int,
    enrl: Int,
    remain: Int,
    capacityXlst: Option[Int],
    enrlXlst: Option[Int],
    remainXlst: Option[Int]
  )

  final case class SwvsectWeb(
    courseInfo: CourseInfo,
    termInfo: TermInfo,
    prereq: Option[String],
    coreq: Option[String],
    dates: Option[String],
    location: Option[String],
    sess: Option[String],
    special: Option[String],
    specialDesc: Option[String],
    text: Option[String],
    bldg: Option[String],
    room: Option[String],
    bnTerm: Option[String],
    crseText: Option[String],
    capacityInfo: CapacityInfo,
    billHours: Double
  )
  object SwvsectWeb {
    implicit def showWeb: Show[SwvsectWeb] = Show.fromToString[SwvsectWeb]
  }

}
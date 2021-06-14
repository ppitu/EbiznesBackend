package models

import play.api.libs.json.{Reads, Writes}

import java.sql.Timestamp

class TimeStampMy {
  implicit val timeStampReads: Reads[Timestamp] = {
    implicitly[Reads[Long]].map(new Timestamp(_))
  }

  implicit val timestampWrites: Writes[Timestamp] = {
    implicitly[Writes[Long]].contramap(_.getTime)
  }
}

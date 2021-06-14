package models

import models.TimeStampMy
import play.api.libs.json.{Json, OFormat}

import java.sql.Timestamp
import java.time.Instant

case class Category(id: Long, name: String, createdAt: Timestamp = Timestamp.from(Instant.now()), updatedAt: Timestamp = Timestamp.from(Instant.now()))

object Category extends TimeStampMy {
  implicit val categoryFormat: OFormat[Category] = Json.using[Json.WithDefaultValues].format[Category]
}

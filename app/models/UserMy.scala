package models

import play.api.libs.json.{Json, OFormat}

import java.sql.Timestamp
import java.time.Instant

case class UserMy(id: Long, name: String, password: String, email: String, creditCardId: Long, addressId: Long, createdAt: Timestamp = Timestamp.from(Instant.now()), updatedAt: Timestamp = Timestamp.from(Instant.now()))

object UserMy extends TimeStampMy {
  implicit val userFormat: OFormat[UserMy] = Json.using[Json.WithDefaultValues].format[UserMy]
}

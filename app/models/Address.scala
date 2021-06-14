package models

import play.api.libs.json.{Json, OFormat}

import java.sql.Timestamp
import java.time.Instant

case class Address(id: Long, street: String, zipcode: String, number: String, city: String, createdAt: Timestamp = Timestamp.from(Instant.now()), updatedAt: Timestamp = Timestamp.from(Instant.now()))

object Address extends TimeStampMy {
  implicit val addressFormat: OFormat[Address] = Json.using[Json.WithDefaultValues].format[Address]
}

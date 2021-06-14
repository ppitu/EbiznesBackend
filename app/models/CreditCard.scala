package models

import play.api.libs.json.{Json, OFormat}

import java.sql.Timestamp
import java.time.Instant

case class CreditCard(id: Long, holderName: String, number: Long, cvv: Long, date: String, createdAt: Timestamp = Timestamp.from(Instant.now()), updatedAt: Timestamp = Timestamp.from(Instant.now()))

object CreditCard extends TimeStampMy {
  implicit val creditCardFormat: OFormat[CreditCard] = Json.using[Json.WithDefaultValues].format[CreditCard]
}
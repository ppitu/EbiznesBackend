package models

import play.api.libs.json.{Json, OFormat}

import java.sql.Timestamp
import java.time.Instant

case class Payment(id: Long, userId: Long, creditCardId: Long, date: String, createdAt: Timestamp = Timestamp.from(Instant.now()), updatedAt: Timestamp = Timestamp.from(Instant.now()))

object Payment extends TimeStampMy {
  implicit val paymentFormat: OFormat[Payment] = Json.using[Json.WithDefaultValues].format[Payment]
}

package models

import play.api.libs.json.{Json, OFormat}

import java.sql.Timestamp
import java.time.Instant

case class Order(id: Long, userId: Long, amount: Float, date: String, createdAt: Timestamp = Timestamp.from(Instant.now()), updatedAt: Timestamp = Timestamp.from(Instant.now()))

object Order extends TimeStampMy {
  implicit val orderFormat: OFormat[Order] = Json.using[Json.WithDefaultValues].format[Order]
}
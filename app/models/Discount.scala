package models

import play.api.libs.json.{Json, OFormat}

import java.sql.Timestamp
import java.time.Instant

case class Discount(id: Long, productId: Long, userId: Long, createdAt: Timestamp = Timestamp.from(Instant.now()), updatedAt: Timestamp = Timestamp.from(Instant.now()))

object Discount extends TimeStampMy {
  implicit val discountFormat: OFormat[Discount] = Json.using[Json.WithDefaultValues].format[Discount]
}

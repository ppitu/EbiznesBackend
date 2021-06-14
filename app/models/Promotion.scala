package models

import play.api.libs.json.{Json, OFormat}

import java.sql.Timestamp
import java.time.Instant

case class Promotion(id: Long, productId: Long, createdAt: Timestamp = Timestamp.from(Instant.now()), updatedAt: Timestamp = Timestamp.from(Instant.now()))

object Promotion extends TimeStampMy {
  implicit val promotionFormat: OFormat[Promotion] = Json.using[Json.WithDefaultValues].format[Promotion]
}
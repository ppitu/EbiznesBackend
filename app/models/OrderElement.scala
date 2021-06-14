package models

import play.api.libs.json.{Json, OFormat}

import java.sql.Timestamp
import java.time.Instant

case class OrderElement(id: Long, productId: Long, createdAt: Timestamp = Timestamp.from(Instant.now()), updatedAt: Timestamp = Timestamp.from(Instant.now()))

object OrderElement extends TimeStampMy {
  implicit val orderElementFormat: OFormat[OrderElement] = Json.using[Json.WithDefaultValues].format[OrderElement]
}
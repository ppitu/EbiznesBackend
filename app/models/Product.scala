package models

import play.api.libs.json.{Json, OFormat}

import java.sql.Timestamp
import java.time.Instant

case class Product(id: Long, name: String, description: String, categoryId: Long, createdAt: Timestamp = Timestamp.from(Instant.now()), updatedAt: Timestamp = Timestamp.from(Instant.now()))

object Product extends TimeStampMy {
  implicit val productFormat: OFormat[Product] = Json.using[Json.WithDefaultValues].format[Product]
}


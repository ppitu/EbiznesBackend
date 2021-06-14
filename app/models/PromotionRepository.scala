package models

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PromotionRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, productRepository: ProductRepository)(implicit ec: ExecutionContext){
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class PromotionTable(tag: Tag) extends Table[Promotion](tag, "promotion") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def productId = column[Long]("product_id", O.Unique)

    def productFk = foreignKey("product_fk", productId, product_)(_.id)

    def createdAt: Rep[Timestamp] = column[Timestamp]("created_at")

    def updatedAt: Rep[Timestamp] = column[Timestamp]("updated_at")

    def * = (id, productId, createdAt, updatedAt) <> ((Promotion.apply _).tupled, Promotion.unapply)
  }

  import productRepository.ProductTable

  private val product_ = TableQuery[ProductTable]
  private val promotion_ = TableQuery[PromotionTable]

  def create(productId: Long, createdAt: Timestamp = Timestamp.from(Instant.now()), updatedAt: Timestamp = Timestamp.from(Instant.now())): Future[Promotion] = db.run {
    (promotion_.map(p => (p.productId, p.createdAt, p.updatedAt))
      returning promotion_.map(_.id)
      into {case ((productId, createdAt, updatedAt), id) => Promotion(id, productId, createdAt, updatedAt)}
      ) += (productId, createdAt, updatedAt)
  }

  def list(): Future[Seq[Promotion]] = db.run {
    promotion_.result
  }

  def getById(id: Long): Future[Promotion] = db.run {
    promotion_.filter(_.id === id).result.head
  }

  def delete(id: Long): Future[Int] = db.run(promotion_.filter(_.id === id).delete)

  def update(id: Long, newPromotion: Promotion): Future[Int] = {
    val promotionToUpdate: Promotion = newPromotion.copy(id)
    db.run(promotion_.filter(_.id === id).update(promotionToUpdate))
  }
}

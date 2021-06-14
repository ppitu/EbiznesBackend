package models

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class DiscountRepository @Inject()(val dbConfigProvider: DatabaseConfigProvider, val productRepository: ProductRepository, val userRepository: UserMyRepository)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class DiscountTable(tag: Tag) extends Table[Discount](tag, "discount") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def productId = column[Long]("product_id")

    def userId = column[Long]("user_id")

    def productFk = foreignKey("prod_fk", productId, product_)(_.id)

    def userFk = foreignKey("user_fk", userId, user_)(_.id)

    def createdAt: Rep[Timestamp] = column[Timestamp]("created_at")

    def updatedAt: Rep[Timestamp] = column[Timestamp]("updated_at")

    def * = (id, productId, userId, createdAt, updatedAt) <> ((Discount.apply _).tupled, Discount.unapply)
  }

  import productRepository.ProductTable
  import userRepository.UserTable

  private val product_ = TableQuery[ProductTable]
  private val user_ = TableQuery[UserTable]
  private val discount_ = TableQuery[DiscountTable]

  def create(productId: Long, userId: Long, createdAt: Timestamp = Timestamp.from(Instant.now()), updatedAt: Timestamp = Timestamp.from(Instant.now())): Future[Discount] = db.run {
    (discount_.map(d => (d.productId, d.userId, d.createdAt, d.updatedAt))
      returning discount_.map(_.id)
      into {case ((productId, userId, createdAt, updatedAt), id) => Discount(id, productId, userId, createdAt, updatedAt)}
      ) += (productId, userId, createdAt, updatedAt)
  }

  def list(): Future[Seq[Discount]] = db.run {
    discount_.result
  }

  def getById(id: Long): Future[Discount] = db.run {
    discount_.filter(_.id === id).result.head
  }

  def delete(id: Long): Future[Int] = db.run(discount_.filter(_.id === id).delete)

  def update(id: Long, newDiscount: Discount): Future[Int] = {
    val discountToUpdate: Discount = newDiscount.copy(id)
    db.run(discount_.filter(_.id === id).update(discountToUpdate))
  }
}
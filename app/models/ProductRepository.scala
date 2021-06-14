package models

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class ProductRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, val categoryRepository: CategoryRepository)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class ProductTable(tag: Tag) extends Table[Product](tag, "product") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name", O.Unique)

    def description = column[String]("description")

    def categoryId = column[Long]("category_id")

    def categoryFk = foreignKey("cat_fk", categoryId, category_)(_.id)

    def createdAt: Rep[Timestamp] = column[Timestamp]("created_at")

    def updatedAt: Rep[Timestamp] = column[Timestamp]("updated_at")

    def * = (id, name, description, categoryId, createdAt, updatedAt) <> ((Product.apply _).tupled, Product.unapply)
  }

  import categoryRepository.CategoryTable

  private val product_ = TableQuery[ProductTable]
  private val category_ = TableQuery[CategoryTable]

  def create(name: String, description: String, categoryId: Long, createdAt: Timestamp = Timestamp.from(Instant.now()), updatedAt: Timestamp = Timestamp.from(Instant.now())): Future[Product] = db.run {
    (product_.map(p => (p.name, p.description, p.categoryId, p.createdAt, p.updatedAt))
      returning product_.map(_.id)
      into {case ((name, description, categoryId, createdAt, updatedAt), id) => Product(id, name, description, categoryId, createdAt, updatedAt)}
      ) += (name, description, categoryId, createdAt, updatedAt)
  }

  def list(): Future[Seq[Product]] = db.run {
    product_.result
  }

  def getById(id: Long): Future[Product] = db.run {
    product_.filter(_.id === id).result.head
  }

  def delete(id: Long): Future[Int] = db.run(product_.filter(_.id === id).delete)

  def update(id: Long, newProduct: Product): Future[Int] = {
    val productToUpdate: Product = newProduct.copy(id)
    db.run(product_.filter(_.id === id).update(productToUpdate))
  }
}

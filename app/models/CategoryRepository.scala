package models

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CategoryRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class CategoryTable(tag: Tag) extends Table[Category](tag, "category") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def createdAt: Rep[Timestamp] = column[Timestamp]("created_at")

    def updatedAt: Rep[Timestamp] = column[Timestamp]("updated_at")

    def * = (id, name, createdAt, updatedAt) <> ((Category.apply _).tupled, Category.unapply)
  }

  val category = TableQuery[CategoryTable]

  def create(name: String, createdAt: Timestamp = Timestamp.from(Instant.now()), updatedAt: Timestamp = Timestamp.from(Instant.now())): Future[Category] = db.run{
    (category.map(c => (c.name, c.createdAt, c.updatedAt))
      returning category.map(_.id)
      into {case ((name, createdAt, updatedAt), id) => Category(id, name, createdAt, updatedAt)}
      ) += (name, createdAt, updatedAt)
  }

  /**
   * List all category in database
   */
  def list(): Future[Seq[Category]] = db.run {
    category.result
  }

  def getById(id: Long): Future[Category] = db.run {
    category.filter(_.id === id).result.head
  }

  def delete(id: Long): Future[Int] = db.run(category.filter(_.id === id).delete)

  def update(id: Long, newCategory: Category): Future[Int] = {
    val categoryToUpdate: Category = newCategory.copy(id)
    db.run(category.filter(_.id === id).update(categoryToUpdate))
  }
}

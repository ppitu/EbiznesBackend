package models

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class OrderRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, val userRepository: UserMyRepository)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class OrderTable(tag: Tag) extends Table[Order](tag, "order") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def userId = column[Long]("user_id")

    def amount = column[Float]("amount")

    def date = column[String]("date")

    def userFk = foreignKey("user_fk", userId, user_)(_.id)

    def createdAt: Rep[Timestamp] = column[Timestamp]("created_at")

    def updatedAt: Rep[Timestamp] = column[Timestamp]("updated_at")

    def * = (id, userId, amount, date, createdAt, updatedAt) <> ((Order.apply _).tupled, Order.unapply)
  }

  import userRepository.UserTable

  private val user_ = TableQuery[UserTable]
  private val order_ = TableQuery[OrderTable]

  def create(userId: Long, amount: Float, date: String, createdAt: Timestamp = Timestamp.from(Instant.now()), updatedAt: Timestamp = Timestamp.from(Instant.now())): Future[Order] = db.run {
    (order_.map(o => (o.userId, o.amount, o.date, o.createdAt, o.updatedAt))
      returning order_.map(_.id)
      into {case ((userId, amount, date, createdAt, updatedAt), id) => Order(id, userId, amount, date, createdAt, updatedAt)}
      ) += (userId, amount, date, createdAt, updatedAt)
  }

  def list(): Future[Seq[Order]] = db.run {
    order_.result
  }

  def getById(id: Long): Future[Order] = db.run {
    order_.filter(_.id === id).result.head
  }

  def delete(id: Long): Future[Int] = db.run(order_.filter(_.id === id).delete)

  def update(id: Long, newOrder: Order): Future[Int] = {
    val orderToUpdate: Order = newOrder.copy(id)
    db.run(order_.filter(_.id === id).update(orderToUpdate))
  }
}

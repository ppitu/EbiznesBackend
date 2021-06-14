package models

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserMyRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext){
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class UserTable(tag: Tag) extends Table[UserMy](tag, "user") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name", O.Unique)

    def password = column[String]("password")

    def email = column[String]("email")

    def creditCardId = column[Long]("credit_card_id")

    def addressId = column[Long]("address_id")

    def createdAt: Rep[Timestamp] = column[Timestamp]("created_at")

    def updatedAt: Rep[Timestamp] = column[Timestamp]("updated_at")

    def * = (id, name, password, email, creditCardId, addressId, createdAt, updatedAt) <> ((UserMy.apply _).tupled, UserMy.unapply)
  }

  private val user_ = TableQuery[UserTable]

  def create(name: String, password: String, email: String, creditCardId: Long, addressId: Long, createdAt: Timestamp = Timestamp.from(Instant.now()), updatedAt: Timestamp = Timestamp.from(Instant.now())): Future[UserMy] = db.run {
    (user_.map(u => (u.name, u.password, u.email, u.creditCardId, u.addressId, u.createdAt, u.updatedAt))
      returning user_.map(_.id)
      into {case ((name, password, email, creditCardId, addressId, createdAt, updatedAt), id) => UserMy(id, name, password, email, creditCardId, addressId, createdAt, updatedAt)}
      ) += (name, password, email, creditCardId, addressId, createdAt, updatedAt)
  }

  def list(): Future[Seq[UserMy]] = db.run {
    user_.result
  }

  def getById(id: Long): Future[UserMy] = db.run {
    user_.filter(_.id === id).result.head
  }

  def delete(id: Long): Future[Int] = db.run(user_.filter(_.id === id).delete)

  def update(id: Long, newUser: UserMy): Future[Int] = {
    val userToUpdate: UserMy = newUser.copy(id)
    db.run(user_.filter(_.id === id).update(userToUpdate))
  }
}

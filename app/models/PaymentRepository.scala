package models

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class PaymentRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, val userRepository: UserMyRepository, val creditCardRepository: CreditCardRepository)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class PaymentTable(tag: Tag) extends Table[Payment](tag, "payment") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def userId = column[Long]("user_id")

    def creditCardId = column[Long]("credit_card_id")

    def date = column[String]("date")

    def userFk = foreignKey("user_fk", userId, user_)(_.id)

    def creditCardFk = foreignKey("cred_fk", creditCardId, creditCard_)(_.id)

    def createdAt: Rep[Timestamp] = column[Timestamp]("created_at")

    def updatedAt: Rep[Timestamp] = column[Timestamp]("updated_at")

    def * = (id, userId, creditCardId, date, createdAt, updatedAt) <> ((Payment.apply _).tupled, Payment.unapply)
  }

  import userRepository.UserTable
  import creditCardRepository.CreditCardTable

  private val user_ = TableQuery[UserTable]
  private val creditCard_ = TableQuery[CreditCardTable]
  private val payment_ = TableQuery[PaymentTable]

  def create(userId: Long, creditCardId: Long, date: String, createdAt: Timestamp = Timestamp.from(Instant.now()), updatedAt: Timestamp = Timestamp.from(Instant.now())): Future[Payment] = db.run {
    (payment_.map(p => (p.userId, p.creditCardId, p.date, p.createdAt, p.updatedAt))
      returning payment_.map(_.id)
      into {case ((userId, creditCardId, date, createdAt, updatedAt), id) => Payment(id, userId, creditCardId, date, createdAt, updatedAt)}
      ) += (userId, creditCardId, date, createdAt, updatedAt)
  }

  def list(): Future[Seq[Payment]] = db.run {
    payment_.result
  }

  def getById(id: Long): Future[Payment] = db.run {
    payment_.filter(_.id === id).result.head
  }

  def delete(id: Long): Future[Int] = db.run(payment_.filter(_.id === id).delete)

  def update(id: Long, newPayment: Payment): Future[Int] = {
    val paymentToUpdate: Payment = newPayment.copy(id)
    db.run(payment_.filter(_.id === id).update(paymentToUpdate))
  }
}

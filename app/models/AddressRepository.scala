package models

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class AddressRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class AddressTable(tag: Tag) extends Table[Address](tag, "address") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def street = column[String]("street")

    def zipcode = column[String]("zipcode")

    def number = column[String]("number")

    def city = column[String]("city")

    def createdAt: Rep[Timestamp] = column[Timestamp]("created_at")

    def updatedAt: Rep[Timestamp] = column[Timestamp]("updated_at")

    def * = (id, street, zipcode, number, city, createdAt, updatedAt) <> ((Address.apply _).tupled, Address.unapply)
  }

  private val address_ = TableQuery[AddressTable]

  def create(street: String, zipcode: String, number: String, city: String, createdAt: Timestamp = Timestamp.from(Instant.now()), updatedAt: Timestamp = Timestamp.from(Instant.now())): Future[Address] = db.run {
    (address_.map(a => (a.street, a.zipcode, a.number, a.city, a.createdAt, a.updatedAt))
      returning address_.map(_.id)
      into {case ((street, zipcode, number, city, createdAt, updatedAt), id) => Address(id, street, zipcode, number, city, createdAt, updatedAt)}
      ) += (street, zipcode, number, city, createdAt, updatedAt)
  }

  def list(): Future[Seq[Address]] = db.run {
    address_.result
  }

  def getById(id: Long): Future[Address] = db.run {
    address_.filter(_.id === id).result.head
  }

  def delete(id: Long): Future[Int] = db.run(address_.filter(_.id === id).delete)

  def update(id: Long, newAddress: Address): Future[Int] = {
    val addressToUpdate: Address = newAddress.copy(id)
    db.run(address_.filter(_.id === id).update(addressToUpdate))
  }
}

package controllers

import akka.actor.ActorSystem
import models.{CreditCard, CreditCardRepository, Payment, PaymentRepository, UserMy, UserMyRepository}
import play.api.data.Forms.{longNumber, mapping, nonEmptyText}
import play.api.data.{Form, Forms}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, MessagesAbstractController, MessagesControllerComponents, MessagesRequest}

import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class PaymentController @Inject()(cc: MessagesControllerComponents, paymentRepository: PaymentRepository, val userRepository: UserMyRepository, val creditCardRepository: CreditCardRepository, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends MessagesAbstractController(cc){
  val _paymentFrom: Form[CreatePaymentForm] = Form {
    mapping(
      "user_id" -> longNumber,
      "creditCard_id" -> longNumber,
      "date" -> nonEmptyText
    )(CreatePaymentForm.apply)(CreatePaymentForm.unapply)
  }

  val _updatePaymentForm: Form[UpdatePaymentForm] = Form {
    mapping(
      "id" -> longNumber,
      "user_id" -> longNumber,
      "creditCard_id" -> longNumber,
      "date" -> nonEmptyText
    )(UpdatePaymentForm.apply)(UpdatePaymentForm.unapply)
  }

  def getPayments: Action[AnyContent] = Action.async { implicit request =>
    val payments = paymentRepository.list()

    payments.map {
      payments =>
        Ok(Json.toJson(payments))
    }
  }

  def getPayment(id: String): Action[AnyContent] = Action.async { implicit request =>
    val payment = paymentRepository.getById(id.toLong)

    payment.map {
      payment =>
        Ok(Json.toJson(payment))
    }
  }

  def updatePayment(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[Payment].map {
      payment =>
        paymentRepository.update(payment.id, payment).map {
          res =>
            Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("invalid json update payment")))
  }

  def deletePayment(id: String): Action[AnyContent] = Action.async { implicit request =>
    paymentRepository.delete(id.toLong).map {
      res =>
        Ok(Json.toJson(res))
    }
  }

  def addPayment(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[Payment].map {
      payment =>
        paymentRepository.create(payment.userId, payment.creditCardId, payment.date).map {
          res =>
            Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("invalid json add payment")))
  }

  def getPaymentsForm: Action[AnyContent] = Action.async { implicit request =>
    val payments = paymentRepository.list()
    payments.map { payment => Ok(views.html.payment.payments(payment))}
  }

  def getPaymentForm(id: String): Action[AnyContent] = Action.async {
    val payment = paymentRepository.getById(id.toLong)

    payment.map {
      case payment: Payment => Ok(views.html.payment.payment(payment))
      case _ => Redirect(routes.PaymentController.getPaymentsForm)
    }
  }

  def addPaymentForm(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val users = Await.result(userRepository.list(), 1.second)
    val creditCards = creditCardRepository.list()

    creditCards.map( creditCard => Ok(views.html.payment.payment_add(_paymentFrom, users, creditCard)))
  }

  def addPaymentHandler(): Action[AnyContent] = Action.async { implicit request =>
    _paymentFrom.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("Error")
        )
      },
      payment => {
        paymentRepository.create(payment.userId, payment.creditCardId, payment.date).map { _ =>
          Redirect(routes.PaymentController.getPaymentsForm).flashing("success" -> "category.created")
        }
      }
    )
  }

  val _userList: Seq[UserMy] = Seq[UserMy]()
  val _creditCardList: Seq[CreditCard] = Seq[CreditCard]()

  def updatePaymentForm(id: String): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val payment = paymentRepository.getById(id.toLong)

    payment.map(payment => {
      val payForm = _updatePaymentForm.fill(UpdatePaymentForm(payment.id, payment.userId, payment.creditCardId, payment.date))

      Ok(views.html.payment.payment_update(payForm, _userList, _creditCardList))
    })
  }

  def updatePaymentHandler(): Action[AnyContent] = Action.async { implicit request =>
    _updatePaymentForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("Error")
        )
      },
      payment => {
        paymentRepository.update(payment.id, Payment(payment.id, payment.userId, payment.creditCardId, payment.date)).map { _ =>
          Redirect(routes.PaymentController.getPaymentsForm).flashing("success" -> "product updated")
        }
      }
    )
  }


}

case class CreatePaymentForm(userId: Long, creditCardId: Long, date: String)
case class UpdatePaymentForm(id: Long, userId: Long, creditCardId: Long, date: String)
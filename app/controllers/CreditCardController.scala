package controllers

import akka.actor.ActorSystem
import models.{CreditCard, CreditCardRepository}
import play.api.data.{Form, Forms}
import play.api.data.Forms.{longNumber, mapping, nonEmptyText}
import play.api.libs.json.{JsValue, Json}

import javax.inject._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class CreditCardController @Inject()(cc: MessagesControllerComponents, creditCardRepository: CreditCardRepository, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends MessagesAbstractController(cc){
  val _creditcardForm: Form[CreateCreditCardForm] = Form {
    mapping(
      "holder_name" -> nonEmptyText,
      "number" -> longNumber,
      "cvv" -> longNumber,
      "date" -> nonEmptyText
    )(CreateCreditCardForm.apply)(CreateCreditCardForm.unapply)
  }

  val _updateCreditCardForm: Form[UpdateCreditCardForm] = Form {
    mapping(
      "id" -> longNumber,
      "holder_name" -> nonEmptyText,
      "number" -> longNumber,
      "cvv" -> longNumber,
      "date" -> nonEmptyText
    )(UpdateCreditCardForm.apply)(UpdateCreditCardForm.unapply)
  }


  def getCreditCards: Action[AnyContent] = Action.async { implicit request =>
    val creditCards = creditCardRepository.list()

    creditCards.map {
      creditCards =>
        Ok(Json.toJson(creditCards))
    }
  }

  def getCreditCard(id: String): Action[AnyContent] = Action.async { implicit request =>
    val creditCard = creditCardRepository.getById(id.toLong)

    creditCard.map {
      creditCard =>
        Ok(Json.toJson(creditCard))
    }
  }

  def updateRCreditCard(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[CreditCard].map {
      creditCard =>
        creditCardRepository.update(creditCard.id, creditCard).map {
          res =>
            Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("invalid json update category")))
  }

  def deleteCreditCard(id: String): Action[AnyContent] = Action.async {
    creditCardRepository.delete(id.toLong).map {
      res =>
        Ok(Json.toJson(res))
    }
  }

  def addCreditCard(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[CreditCard].map {
      creditCard =>
        creditCardRepository.create(creditCard.holderName, creditCard.number, creditCard.cvv, creditCard.date).map {
          res =>
            Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("invalid json update category")))
  }

  def getCreditCardsForm: Action[AnyContent] = Action.async { implicit request =>
    val creditcards = creditCardRepository.list()
    creditcards.map { creditcard => Ok(views.html.creditcard.creditcards(creditcard))}
  }

  def getCreditCardForm(id: String): Action[AnyContent] = Action.async {
    val creditcard = creditCardRepository.getById(id.toLong)

    creditcard.map {
      case creditcard: CreditCard => Ok(views.html.creditcard.creditcard(creditcard))
      case _ => Redirect(routes.CreditCardController.getCreditCardsForm)
    }
  }

  def addCreditCardForm(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    Future(Ok(views.html.creditcard.creditcard_add(_creditcardForm)))
  }

  def addCreditCardHandler(): Action[AnyContent] = Action.async { implicit request =>
    _creditcardForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("Error")
        )
      },
      creditCard => {
        creditCardRepository.create(creditCard.holderName, creditCard.number, creditCard.cvv, creditCard.date).map { _ =>
          Redirect(routes.CreditCardController.getCreditCardsForm).flashing("success" -> "category.created")
        }
      }
    )
  }

  def updateCreditCardForm(id: String): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val creditCard = creditCardRepository.getById(id.toLong)

    creditCard.map(creditCard => {
      val creForm = _updateCreditCardForm.fill(UpdateCreditCardForm(creditCard.id, creditCard.holderName, creditCard.number, creditCard.cvv, creditCard.date))

      Ok(views.html.creditcard.creditcard_update(creForm))
    })
  }

  def updateCreditCardHandler(): Action[AnyContent] = Action.async { implicit request =>
    _updateCreditCardForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("Error")
        )
      },
      creditCard => {
        creditCardRepository.update(creditCard.id, CreditCard(creditCard.id, creditCard.holderName, creditCard.number, creditCard.cvv, creditCard.date)).map { _ =>
          Redirect(routes.CreditCardController.getCreditCardsForm).flashing("success" -> "product updated")
        }
      }
    )
  }

}

case class CreateCreditCardForm(holderName: String, number: Long, cvv: Long, date: String)
case class UpdateCreditCardForm(id: Long, holderName: String, number: Long, cvv: Long, date: String)
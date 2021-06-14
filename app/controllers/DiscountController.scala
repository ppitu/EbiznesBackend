package controllers

import akka.actor.ActorSystem
import models.{Discount, DiscountRepository, Product, ProductRepository, UserMy, UserMyRepository}
import play.api.libs.json.{JsValue, Json}
import javax.inject._
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping}
import play.api.mvc._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

class DiscountController @Inject()(cc: MessagesControllerComponents, discountRepository: DiscountRepository, val productRepository: ProductRepository, val userRepository: UserMyRepository, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends MessagesAbstractController(cc){
  val _discountFrom: Form[CreateDiscountForm] = Form {
    mapping(
      "product_id" -> longNumber,
      "user_id" -> longNumber,
    )(CreateDiscountForm.apply)(CreateDiscountForm.unapply)
  }

  val _updateDiscountForm: Form[UpdateDiscountForm] = Form {
    mapping(
      "id" -> longNumber,
      "product_id" -> longNumber,
      "user_id" -> longNumber
    )(UpdateDiscountForm.apply)(UpdateDiscountForm.unapply)
  }


  def getDiscounts: Action[AnyContent] = Action.async { implicit request =>
    val discounts = discountRepository.list()

    discounts.map {
      discounts =>
        Ok(Json.toJson(discounts))
    }
  }

  def getDiscount(id: String): Action[AnyContent] = Action.async { implicit request =>
    val discount = discountRepository.getById(id.toLong)

    discount.map {
      discount =>
        Ok(Json.toJson(discount))
    }
  }

  def updateDiscount(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[Discount].map {
      discount =>
        discountRepository.update(discount.id, discount).map { res =>
          Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("invalid json update discount")))
  }

  def deleteDiscount(id: String): Action[AnyContent] = Action.async { implicit request =>
    discountRepository.delete(id.toLong).map {
      res =>
        Ok(Json.toJson(res))
    }
  }

  def addDiscount(): Action[JsValue] = Action.async(parse.json) { implicit  request =>
    request.body.validate[Discount].map {
      discount =>
        discountRepository.create(discount.productId, discount.userId).map {
          res =>
            Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("invalid json add discount")))
  }

  def getDiscountsForm: Action[AnyContent] = Action.async { implicit request =>
    val discounts = discountRepository.list()
    discounts.map { discount => Ok(views.html.discount.discounts(discount))}
  }

  def getDiscountForm(id: String): Action[AnyContent] = Action.async {
    val discount = discountRepository.getById(id.toLong)

    discount.map {
      case discount: Discount => Ok(views.html.discount.discount(discount))
      case _ => Redirect(routes.DiscountController.getDiscountsForm)
    }
  }

  def addDiscountForm(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val products = Await.result(productRepository.list(), 1.second)
    val users = userRepository.list()

    users.map( user => Ok(views.html.discount.discount_add(_discountFrom, user,  products)))
  }

  def addDiscountHandler(): Action[AnyContent] = Action.async { implicit request =>
    _discountFrom.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("Error")
        )
      },
      discount => {
        discountRepository.create(discount.productId, discount.userId).map { _ =>
          Redirect(routes.DiscountController.getDiscountsForm).flashing("success" -> "category.created")
        }
      }
    )
  }

  val _userList: Seq[UserMy] = Seq[UserMy]()
  val _productList: Seq[Product] = Seq[Product]()

  def updateDiscountForm(id: String): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val discount = discountRepository.getById(id.toLong)

    discount.map(discount => {
      val disForm = _updateDiscountForm.fill(UpdateDiscountForm(discount.id, discount.productId, discount.userId))

      Ok(views.html.discount.discount_update(disForm, _userList, _productList))
    })
  }

  def updateDiscountHandler(): Action[AnyContent] = Action.async { implicit request =>
    _updateDiscountForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("Error")
        )
      },
      discount => {
        discountRepository.update(discount.id, Discount(discount.id, discount.productId, discount.userId)).map { _ =>
          Redirect(routes.DiscountController.getDiscountsForm).flashing("success" -> "product updated")
        }
      }
    )
  }

}

case class CreateDiscountForm(productId: Long, userId: Long)
case class UpdateDiscountForm(id: Long, productId: Long, userId: Long)
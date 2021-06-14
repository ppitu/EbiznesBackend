package controllers

import akka.actor.ActorSystem
import models.{OrderElement, OrderElementRepository, Product, ProductRepository}
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping}
import play.api.libs.json.{JsValue, Json}

import javax.inject._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class OrderElementController @Inject()(cc: MessagesControllerComponents, orderElementRepository: OrderElementRepository, val productRepository: ProductRepository, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends MessagesAbstractController(cc){

  val _orderElementForm: Form[CreateOrderElementForm] = Form {
    mapping(
      "product_id" -> longNumber
    )(CreateOrderElementForm.apply)(CreateOrderElementForm.unapply)
  }

  val _updateOrderElementForm: Form[UpdateOrderElementForm] = Form {
    mapping(
      "id" -> longNumber,
      "product_id" -> longNumber
    )(UpdateOrderElementForm.apply)(UpdateOrderElementForm.unapply)
  }

  def getOrderElements: Action[AnyContent] = Action.async { implicit request =>
    val orderElements = orderElementRepository.list()

    orderElements.map {
      orderElements =>
        Ok(Json.toJson(orderElements))
    }
  }

  def getOrderElement(id: String): Action[AnyContent] = Action.async { implicit request =>
    val orderElement = orderElementRepository.getById(id.toLong)

    orderElement.map {
      orderElement =>
        Ok(Json.toJson(orderElement))
    }
  }

  def updateOrderElement(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[OrderElement].map {
      orderElement =>
        orderElementRepository.update(orderElement.id, orderElement).map {
          res =>
            Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("invalid json update orderElement")))
  }

  def deleteOrderElement(id: String): Action[AnyContent] = Action.async { implicit request =>
    orderElementRepository.delete(id.toLong).map {
      res =>
        Ok(Json.toJson(res))
    }
  }

  def addOrderElement(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[OrderElement].map {
      orderElement =>
        orderElementRepository.create(orderElement.productId).map {
          res =>
            Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("invalid json add orderelement")))
  }

  def getOrderElementsForm: Action[AnyContent] = Action.async { implicit request =>
    val orderelements = orderElementRepository.list()
    orderelements.map { orderelement => Ok(views.html.orderelement.orderelements(orderelement))}
  }

  def getOrderElementForm(id: String): Action[AnyContent] = Action.async {
    val orderelement = orderElementRepository.getById(id.toLong)

    orderelement.map {
      case orderelement: OrderElement => Ok(views.html.orderelement.orderelement(orderelement))
      case _ => Redirect(routes.OrderElementController.getOrderElementsForm)
    }
  }

  def addOrderElementForm(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val products = productRepository.list()

    products.map( product => Ok(views.html.orderelement.orderelement_add(_orderElementForm, product)))
  }

  def addOrderElementHandler(): Action[AnyContent] = Action.async { implicit request =>
    _orderElementForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("Error")
        )
      },
      orderelement => {
        orderElementRepository.create(orderelement.productId).map { _ =>
          Redirect(routes.OrderElementController.getOrderElementsForm).flashing("success" -> "category.created")
        }
      }
    )
  }

  val _productList: Seq[Product] = Seq[Product]()

  def updateOrderElementForm(id: String): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val orderelement = orderElementRepository.getById(id.toLong)

    orderelement.map(orderelement => {
      val ordForm = _updateOrderElementForm.fill(UpdateOrderElementForm(orderelement.id, orderelement.productId))

      Ok(views.html.orderelement.orderelement_update(ordForm, _productList))
    })
  }

  def updateOrderElementHandler(): Action[AnyContent] = Action.async { implicit request =>
    _updateOrderElementForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("Error")
        )
      },
      orderelement => {
        orderElementRepository.update(orderelement.id, OrderElement(orderelement.id, orderelement.productId)).map { _ =>
          Redirect(routes.OrderElementController.getOrderElementsForm).flashing("success" -> "product updated")
        }
      }
    )
  }
}

case class CreateOrderElementForm(productId: Long)
case class UpdateOrderElementForm(id: Long, productId: Long)
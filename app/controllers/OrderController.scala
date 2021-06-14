package controllers

import akka.actor.ActorSystem
import models.{Order, OrderRepository, UserMy, UserMyRepository}
import play.api.libs.json.{JsValue, Json}
import javax.inject._
import play.api.data.{Form, Forms}
import play.api.data.Forms.{bigDecimal, longNumber, mapping, nonEmptyText, number}
import play.api.data.format.Formats.floatFormat
import play.api.mvc._

import scala.concurrent.{Await, ExecutionContext, Future}

class OrderController @Inject()(cc: MessagesControllerComponents, orderRepository: OrderRepository, val userRepository: UserMyRepository, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends MessagesAbstractController(cc){
  val _orderFrom: Form[CreateOrderForm] = Form {
    mapping(
      "user_id" -> longNumber,
      "amount" -> Forms.of[Float],
      "date" -> nonEmptyText
    )(CreateOrderForm.apply)(CreateOrderForm.unapply)
  }

  val _updateOrderForm: Form[UpdateOrderForm] = Form {
    mapping(
      "id" -> longNumber,
      "user_id" -> longNumber,
      "amount" -> Forms.of[Float],
      "date" -> nonEmptyText
    )(UpdateOrderForm.apply)(UpdateOrderForm.unapply)
  }


  def getOrders: Action[AnyContent] = Action.async { implicit request =>
    val orders = orderRepository.list()

    orders.map {
      orders =>
        Ok(Json.toJson(orders))
    }
  }

  def getOrder(id: String): Action[AnyContent] = Action.async { implicit request =>
    val order = orderRepository.getById(id.toLong)

    order.map {
      order =>
        Ok(Json.toJson(order))
    }
  }

  def updateOrder(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[Order].map {
      order =>
        orderRepository.update(order.id, order).map {
          res =>
            Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("invalid json update order")))
  }

  def deleteOrder(id: String): Action[AnyContent] = Action.async { implicit request =>
    orderRepository.delete(id.toLong).map {
      res =>
        Ok(Json.toJson(res))
    }

  }

  def addOrder(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[Order].map {
      order =>
        orderRepository.create(order.userId, order.amount, order.date).map {
          res =>
            Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("invalid json add order")))
  }

  def getOrdersForm: Action[AnyContent] = Action.async { implicit request =>
    val orders = orderRepository.list()
    orders.map { order => Ok(views.html.order.orders(order))}
  }

  def getOrderForm(id: String): Action[AnyContent] = Action.async {
    val order = orderRepository.getById(id.toLong)

    order.map {
      case order: Order => Ok(views.html.order.order(order))
      case _ => Redirect(routes.OrderController.getOrdersForm)
    }
  }

  def addOrderForm(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val users = userRepository.list()

    users.map( user => Ok(views.html.order.order_add(_orderFrom, user)))
  }

  def addOrderHandler(): Action[AnyContent] = Action.async { implicit request =>
    _orderFrom.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("Error")
        )
      },
      order => {
        orderRepository.create(order.userId, order.amount, order.date).map { _ =>
          Redirect(routes.OrderController.getOrdersForm).flashing("success" -> "category.created")
        }
      }
    )
  }

  val _userList: Seq[UserMy] = Seq[UserMy]()

  def updateOrderForm(id: String): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val order = orderRepository.getById(id.toLong)

    order.map(order => {
      val ordForm = _updateOrderForm.fill(UpdateOrderForm(order.id, order.userId, order.amount, order.date))

      Ok(views.html.order.order_update(ordForm, _userList))
    })
  }

  def updateProductHandler(): Action[AnyContent] = Action.async { implicit request =>
    _updateOrderForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("Error")
        )
      },
      order => {
        orderRepository.update(order.id, Order(order.id, order.userId, order.amount, order.date)).map { _ =>
          Redirect(routes.OrderController.getOrdersForm).flashing("success" -> "product updated")
        }
      }
    )
  }


}

case class CreateOrderForm(userId: Long, amount: Float, date: String)
case class UpdateOrderForm(id: Long, userId: Long, amount: Float, date: String)
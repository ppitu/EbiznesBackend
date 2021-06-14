package controllers

import akka.actor.ActorSystem
import models.{ProductRepository, Promotion, PromotionRepository, Product}
import play.api.data.{Form, Forms}
import play.api.data.Forms.{longNumber, mapping}
import play.api.libs.json.{JsValue, Json}

import javax.inject._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future }

class PromotionController @Inject()(cc: MessagesControllerComponents, promotionRepository: PromotionRepository, val productRepository: ProductRepository, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends MessagesAbstractController(cc){
  val _promotionForm: Form[CreatePromotionForm] = Form {
    mapping(
      "product_id" -> longNumber
    )(CreatePromotionForm.apply)(CreatePromotionForm.unapply)
  }

  val _updatePromotionForm: Form[UpdatePromotionForm] = Form {
    mapping(
      "id" -> longNumber,
      "product_id" -> longNumber
    )(UpdatePromotionForm.apply)(UpdatePromotionForm.unapply)
  }



  def getPromotions: Action[AnyContent] = Action.async { implicit request =>
    val promotions = promotionRepository.list()

    promotions.map {
      promotions =>
        Ok(Json.toJson(promotions))
    }
  }

  def getPromotion(id: String): Action[AnyContent] = Action.async { implicit request =>
    val promotion = promotionRepository.getById(id.toLong)

    promotion.map {
      promotion =>
        Ok(Json.toJson(promotion))
    }
  }

  def updatePromotion(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[Promotion].map {
      promotion =>
        promotionRepository.update(promotion.id, promotion).map {
          res =>
            Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("invalid json update promotions")))
  }

  def deletePromotion(id: String): Action[AnyContent] = Action.async { implicit request =>
    promotionRepository.delete(id.toLong).map { res =>
      Ok(Json.toJson(res))
    }
  }

  def addPromotion(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[Promotion].map {
      promotion =>
        promotionRepository.create(promotion.productId).map {
          res =>
            Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("invalid json add promotions")))
  }

  def getPromotionsForm: Action[AnyContent] = Action.async { implicit request =>
    val promotions = promotionRepository.list()
    promotions.map { promotion => Ok(views.html.promotion.promotions(promotion))}
  }

  def getPromotionForm(id: String): Action[AnyContent] = Action.async {
    val promotion = promotionRepository.getById(id.toLong)

    promotion.map {
      case promotion: Promotion => Ok(views.html.promotion.promotion(promotion))
      case _ => Redirect(routes.PromotionController.getPromotionsForm)
    }
  }

  def addPromotionForm(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val products = productRepository.list()

    products.map( product => Ok(views.html.promotion.promotion_add(_promotionForm, product)))
  }

  def addPromotionHandler(): Action[AnyContent] = Action.async { implicit request =>
    _promotionForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("Error")
        )
      },
      promotion => {
        promotionRepository.create(promotion.productId).map { _ =>
          Redirect(routes.PromotionController.getPromotionsForm).flashing("success" -> "category.created")
        }
      }
    )
  }

  val _productList: Seq[Product] = Seq[Product]()

  def updatePromotionForm(id: String): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val promotion = promotionRepository.getById(id.toLong)

    promotion.map(promotion => {
      val proForm = _updatePromotionForm.fill(UpdatePromotionForm(promotion.id, promotion.productId))

      Ok(views.html.promotion.promotion_update(proForm, _productList))
    })
  }

  def updatePromotionHandler(): Action[AnyContent] = Action.async { implicit request =>
    _updatePromotionForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("Error")
        )
      },
      promotion => {
        promotionRepository.update(promotion.id, Promotion(promotion.id, promotion.productId)).map { _ =>
          Redirect(routes.PromotionController.getPromotionsForm).flashing("success" -> "product updated")
        }
      }
    )
  }

}

case class CreatePromotionForm(productId: Long)
case class UpdatePromotionForm(id: Long, productId: Long)
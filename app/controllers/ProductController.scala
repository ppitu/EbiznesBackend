package controllers

import akka.actor.ActorSystem
import models.{Category, CategoryRepository, Product, ProductRepository, UserMy}
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping, nonEmptyText, number}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, MessagesAbstractController, MessagesControllerComponents, MessagesRequest}
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future, promise}
import scala.util.{Failure, Success}

@Singleton
class ProductController @Inject()(cc: MessagesControllerComponents, val categoryRepository: CategoryRepository, val productRepository: ProductRepository)(implicit exec: ExecutionContext) extends MessagesAbstractController(cc){

  val productForm: Form[CreateProductForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "category" -> longNumber,
    )(CreateProductForm.apply)(CreateProductForm.unapply)
  }

  val _updateProductForm: Form[UpdateProductForm] = Form {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "category" -> longNumber
    )(UpdateProductForm.apply)(UpdateProductForm.unapply)
  }

  def getProducts: Action[AnyContent] = Action.async {
    val products = productRepository.list()
    products.map {
      products =>
        Ok(Json.toJson(products))
    }
  }

  def getProductsForm: Action[AnyContent] = Action.async { implicit request =>
    val products = productRepository.list()
    products.map { product => Ok(views.html.product.products(product))}
  }

  def getProduct(id: String): Action[AnyContent] = Action.async {
    val product = productRepository.getById(id.toLong)

    product.map {
      product => Ok(Json.toJson(product))
    }
  }

  def getProductForm(id: String): Action[AnyContent] = Action.async {
    val product = productRepository.getById(id.toLong)

    product.map {
      case product: Product => Ok(views.html.product.product(product))
      case _ => Redirect(routes.ProductController.getProductsForm)
    }
  }

  def updateProduct(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[Product].map {
      product =>
        productRepository.update(product.id, product).map {
          res =>
            Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("invalid json update product")))
  }

  def deleteProduct(id: String): Action[AnyContent] = Action.async {
    productRepository.delete(id.toLong).map {
      res =>
        Ok(Json.toJson(res))
    }
  }

  def addProduct(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[Product].map {
      product => {
        productRepository.create(product.name, product.description, product.categoryId).map {
          res =>
            Ok(Json.toJson(res))
        }
      }
    }.getOrElse(Future.successful(BadRequest("invalid json add product")))
  }

  def addProductForm(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val categories = categoryRepository.list()
    categories.map(cat => Ok(views.html.product.products_add(productForm, cat)))
  }

  def addProductHandler(): Action[AnyContent] = Action.async { implicit request =>
    productForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("Error")
        )
      },
      product => {
        productRepository.create(product.name, product.description, product.category).map { _ =>
          Redirect(routes.ProductController.getProductsForm).flashing("success" -> "category.created")
        }
      }
    )
  }

  def updateProductForm(id: String): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var categ:Seq[Category] = Seq[Category]()
    categoryRepository.list().onComplete{
      case Success(cat) => categ = cat
      case Failure(_) => print("fail")
    }

    val product = productRepository.getById(id.toLong)

    product.map(product => {
      val prodForm = _updateProductForm.fill(UpdateProductForm(product.id, product.name, product.description, product.categoryId))

      Ok(views.html.product.products_update(prodForm, categ))
    })
  }

  def updateProductHandler(): Action[AnyContent] = Action.async { implicit request =>
    _updateProductForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("Error")
        )
      },
      product => {
        productRepository.update(product.id, Product(product.id, product.name, product.description, product.category)).map { _ =>
          Redirect(routes.ProductController.getProductsForm).flashing("success" -> "product updated")
        }
      }
    )
  }
}

case class CreateProductForm(name: String, description: String, category: Long)
case class UpdateProductForm(id: Long, name: String, description: String, category: Long)

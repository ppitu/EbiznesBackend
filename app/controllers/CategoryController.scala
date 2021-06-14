package controllers

import models.{Category, CategoryRepository}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping, nonEmptyText}
import play.api.http.HeaderNames
import play.api.libs.json.{JsValue, Json}

import javax.inject._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CategoryController @Inject()(cc: MessagesControllerComponents, val categoryRepository: CategoryRepository,
                                   val messagesActionBuilder: MessagesActionBuilder)(implicit exec: ExecutionContext) extends MessagesAbstractController(cc){

  val logger: Logger = Logger(classOf[CategoryController])

  val categoryForm: Form[CreateCategoryForm] = Form {
    mapping(
      "name" -> nonEmptyText
    )(CreateCategoryForm.apply)(CreateCategoryForm.unapply)
  }

  val updateCategoryForm: Form[UpdateCategoryForm] = Form {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText
    )(UpdateCategoryForm.apply)(UpdateCategoryForm.unapply)
  }

  def getCategories: Action[AnyContent] = Action.async { implicit request =>
    val categories = categoryRepository.list()
    categories.map {
      categories =>
        Ok(Json.toJson(categories))
    }
  }

  def getCategoriesForm: Action[AnyContent] = Action.async { implicit request =>
    val category = categoryRepository.list()
    category.map {
      category =>
        Ok(views.html.category.categories(category))
    }

  }

  def getCategory(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val category = categoryRepository.getById(id)
    category.map {
      case category: Category => Ok(Json.toJson(category))
      case _ => Redirect(routes.CategoryController.getCategories)
    }
  }

  def getCategoryForm(id: String): Action[AnyContent] = Action.async { implicit request =>
    val category = categoryRepository.getById(id.toLong)

    category.map {
      case category: Category => Ok(views.html.category.category(category))
      case _ => Redirect(routes.CategoryController.getCategories)
    }
  }

  def updateCategory(id: String): Action[JsValue] = Action.async(parse.json) { request => //implicit request:MessagesRequest[AnyContent] =>

    request.body.validate[Category].map {
      category =>
        categoryRepository.update(category.id, category).map {
          res =>
            Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("invalid json update category")))
  }

  def updateFormCategory(id: String): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val category = categoryRepository.getById(id.toLong)

    category.map(category => {
      val catForm = updateCategoryForm.fill(UpdateCategoryForm(category.id, category.name))

      Ok(views.html.category.category_update(catForm))
    })
  }

  def updateCategoryHandler(): Action[AnyContent] = Action.async { implicit request =>
    updateCategoryForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("Error")
        )
      },
      category => {
        categoryRepository.update(category.id, Category(category.id, category.name)).map { _ =>
          Redirect(routes.CategoryController.getCategoriesForm).flashing("success" -> "category updates")
        }
      }
    )
  }


  def deleteCategory(id: String): Action[AnyContent] = Action.async {
    categoryRepository.delete(id.toLong).map { res =>
      Ok(Json.toJson(res))
    }
  }

  def addCategory(): Action[JsValue] = Action.async(parse.json) { implicit request => //messagesActionBuilder { implicit request: MessagesRequest[AnyContent] =>
    //Ok(views.html.category.category_add(categoryForm))

    request.body.validate[Category].map {
      category =>
        categoryRepository.create(category.name).map {
          res =>
            Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("invalid json add category")))
  }

  def addCategoryForm(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    Future(Ok(views.html.category.category_add(categoryForm)))
  }

  def addCategoryHandler(): Action[AnyContent] = Action.async{ implicit request =>
    categoryForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("Error")
        )
      },
      category => {
        categoryRepository.create(category.name).map { _ =>
          Redirect(routes.CategoryController.addCategory).flashing("success" -> "category.created")
        }
      }
    )
  }
}

case class CreateCategoryForm(name: String)
case class UpdateCategoryForm(id: Long, name: String)
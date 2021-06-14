package controllers

import akka.actor.ActorSystem
import models.{Address, AddressRepository}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping, nonEmptyText}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, MessagesAbstractController, MessagesControllerComponents, MessagesRequest}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressController @Inject()(cc: MessagesControllerComponents, addressRepository: AddressRepository, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends MessagesAbstractController(cc){

  val _addressForm: Form[CreateAddressForm] = Form {
    mapping(
      "street" -> nonEmptyText,
      "zipcode" -> nonEmptyText,
      "number" -> nonEmptyText,
      "city" -> nonEmptyText
    )(CreateAddressForm.apply)(CreateAddressForm.unapply)
  }

  val _updateAddressForm: Form[UpdateAddressForm] = Form {
    mapping(
      "id" -> longNumber,
      "street" -> nonEmptyText,
      "zipcode" -> nonEmptyText,
      "number" -> nonEmptyText,
      "city" -> nonEmptyText
    )(UpdateAddressForm.apply)(UpdateAddressForm.unapply)
  }

  def getAddresses: Action[AnyContent] = Action.async { request =>
    val addresses = addressRepository.list()

    addresses.map {
      addresses =>
        Ok(Json.toJson(addresses))
    }
  }

  def getAddress(id: String): Action[AnyContent] = Action.async {
    val address = addressRepository.getById(id.toLong)

    address.map {
      address =>
        Ok(Json.toJson(address))
    }
  }

  def updateAddress(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[Address].map {
      address =>
        addressRepository.update(address.id, address).map {
          res =>
            Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("invalid json add address")))
  }

  def deleteAddress(id: String): Action[AnyContent] = Action.async {
    addressRepository.delete(id.toLong).map { res =>
      Ok(Json.toJson(res))
    }
  }

  def addAddress(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[Address].map {
      address => {
        addressRepository.create(address.street, address.zipcode, address.number, address.city).map {
          res =>
            Ok(Json.toJson(res))
        }
      }
    }.getOrElse(Future.successful(BadRequest("invalid json add address")))
  }

  def getAddressesForm: Action[AnyContent] = Action.async { implicit request =>
    val addresses = addressRepository.list()
    addresses.map { address => Ok(views.html.address.addresses(address))}
  }

  def getAddressForm(id: String): Action[AnyContent] = Action.async {
    val address = addressRepository.getById(id.toLong)

    address.map {
      case address: Address => Ok(views.html.address.address(address))
      case _ => Redirect(routes.AddressController.getAddressesForm)
    }
  }

  def addAddressForm(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    Future(Ok(views.html.address.address_add(_addressForm)))
  }

  def addAddressHandler(): Action[AnyContent] = Action.async { implicit request =>
    _addressForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("Error")
        )
      },
      address => {
        addressRepository.create(address.street, address.zipcode, address.number, address.city).map { _ =>
          Redirect(routes.AddressController.getAddressesForm).flashing("success" -> "category.created")
        }
      }
    )
  }

  def updateAddressForm(id: String): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val address = addressRepository.getById(id.toLong)

    address.map(address => {
      val addForm = _updateAddressForm.fill(UpdateAddressForm(address.id, address.street, address.zipcode, address.number, address.city))

      Ok(views.html.address.address_update(addForm))
    })
  }

  def updateAddressHandler(): Action[AnyContent] = Action.async { implicit request =>
    _updateAddressForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("Error")
        )
      },
      address => {
        addressRepository.update(address.id, Address(address.id, address.street, address.zipcode, address.number, address.city)).map { _ =>
          Redirect(routes.AddressController.getAddressesForm).flashing("success" -> "product updated")
        }
      }
    )
  }

}

case class CreateAddressForm(street: String, zipcode: String, number: String, city: String)
case class UpdateAddressForm(id: Long, street: String, zipcode: String, number: String, city: String)
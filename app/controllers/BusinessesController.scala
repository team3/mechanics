package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import dao.MongoDbBusinessesRepository
import model.Business
import modules.JWTEnv
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsError, Json, _}
import play.api.mvc._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BusinessesController @Inject()(val reactiveMongoApi: ReactiveMongoApi,
                                     val messagesApi: MessagesApi,
                                     val businessesRepository: MongoDbBusinessesRepository,
                                     silhouette: Silhouette[JWTEnv]) extends Controller {

  private[BusinessesController] def display(objects: Future[List[JsObject]]) =
    objects
      .map(obj => Ok(obj.map(b => b.values).mkString(",")))
      .recover { case e: Exception => BadRequest(e.getMessage) }

  def search(term: String) = Action.async { implicit request =>
    display(businessesRepository.find(term))
  }

  def all = Action.async { implicit request =>
    display(businessesRepository.find(""))
  }

  def save(id: String) = silhouette.SecuredAction.async { implicit request =>
    import model.Business.Fields._

    request.body.asJson.map { r =>
      r.validate[Business] match {
        case JsSuccess(business, _) =>
          val document = BSONDocument(
            Name -> business.name,
            Address -> business.address,
            Country -> business.country,
            City -> business.city,
            Email -> business.email,
            Phone -> business.phone
          )

          businessesRepository.update(BSONDocument(Id -> BSONObjectID(id)), document)
            .map(result => Ok(Json.obj("success" -> result.ok)))
            .recover {
              case e: Exception => Ok(Json.obj("business error" -> e.getMessage))
            }
        case err@JsError(_) => Future.successful(BadRequest(JsError.toJson(err)))
      }
    }.getOrElse(Future.successful(BadRequest("Invalid request")))
  }
}



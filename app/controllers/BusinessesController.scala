package controllers

import javax.inject.Inject

import dao.MongoDbBusinessesRepository
import model.Business
import play.api.libs.json.Json
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global

class BusinessesController @Inject()(val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents {

  val businessesRepository = new MongoDbBusinessesRepository(reactiveMongoApi)

  def search(term: String) = Action.async { implicit request =>
    businessesRepository.find()
      .map(businesses => Ok("" + businesses.map(b => b.values)))
      .recover { case _ => BadRequest("Error") }
  }

  def save(id: String) = Action.async { implicit request =>
    import Business.Fields._

    val business = request.body.asJson.get.as[Business]
    val document = BSONDocument(
      Name -> business.name,
      Address -> business.address,
      Country -> business.country,
      City -> business.city,
      Email -> business.email,
      Phone -> business.phone
    )

    businessesRepository.update(BSONDocument(Id -> BSONObjectID(id)), document)
      .map(le => Ok(Json.obj("success" -> le.ok)))
  }
}
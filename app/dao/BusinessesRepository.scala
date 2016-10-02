package dao

import javax.inject.Inject

import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.ReadPreference
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait BusinessesRepository {
  def find(): Future[List[JsObject]]

  def create(document: BSONDocument): Future[WriteResult]

  def update(old: BSONDocument, document: BSONDocument): Future[WriteResult]
}

class MongoDbBusinessesRepository @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends BusinessesRepository {

  import play.modules.reactivemongo.json._

  protected val collection = reactiveMongoApi.db.collection[JSONCollection]("businesses")

  override def find(): Future[List[JsObject]] =
    collection.find(Json.obj())
      .cursor[JsObject](ReadPreference.Primary)
      .collect[List]()

  override def create(document: BSONDocument): Future[WriteResult] = {
    collection.update(BSONDocument("_id" -> document.get("_id").getOrElse(BSONObjectID.generate)), document, upsert = true)
  }

  override def update(old: BSONDocument, document: BSONDocument): Future[WriteResult] = {
    collection.update(old, document)
  }
}

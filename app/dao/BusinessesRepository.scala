package dao

import javax.inject.Inject

import play.api.libs.json.{JsObject, Json}
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{DB, ReadPreference}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait BusinessesRepository {
  def find(term: String): Future[List[JsObject]]

  def create(document: BSONDocument): Future[WriteResult]

  def update(old: BSONDocument, document: BSONDocument): Future[WriteResult]
}

class MongoDbBusinessesRepository @Inject()(db: DB) extends BusinessesRepository {

  import play.modules.reactivemongo.json._

  protected val collection = db.collection[JSONCollection]("businesses")

  override def find(term: String): Future[List[JsObject]] =
    collection.find(Json.obj("$text" -> Json.obj("$search" -> term)))
      .cursor[JsObject](ReadPreference.Primary)
      .collect[List]()

  override def create(document: BSONDocument): Future[WriteResult] = {
    collection.update(BSONDocument("_id" -> document.get("_id").getOrElse(BSONObjectID.generate)), document, upsert = true)
  }

  override def update(old: BSONDocument, document: BSONDocument): Future[WriteResult] = {
    collection.update(old, document)
  }
}

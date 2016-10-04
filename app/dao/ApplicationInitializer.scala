package dao

import javax.inject.Inject

import play.api.libs.json.Json
import play.api.{Application, GlobalSettings, Logger}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global

class ApplicationInitializer @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends GlobalSettings {

  def collection = reactiveMongoApi.db.collection[JSONCollection]("businesses")

  val businesses = List(
    Json.obj(
      "name" -> "B1",
      "address" -> "Foa 1",
      "country" -> "USA",
      "city" -> "New York",
      "email" -> "b1@businesses.com",
      "phone" -> 1201384781
    ),
    Json.obj(
      "name" -> "B2",
      "address" -> "Foa 2",
      "country" -> "USA",
      "city" -> "New York",
      "email" -> "b2@businesses.com",
      "phone" -> 1201384782
    )
  )

  override def onStart(app: Application) {
    collection.bulkInsert(businesses.toStream, ordered = true).
      foreach(i => Logger.info("Database was initialized"))
  }

  override def onStop(app: Application) {
    collection.drop().onComplete {
      case _ => Logger.info("Database collection dropped")
    }
  }
}

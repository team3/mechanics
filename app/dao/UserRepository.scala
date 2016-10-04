package dao

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import model.User
import play.api.libs.json.Json
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.api.DB

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.modules.reactivemongo.json._

trait UserRepository {
  def find(loginInfo: LoginInfo): Future[Option[User]]

  def find(userId: UUID): Future[Option[User]]

  def save(user: User): Future[User]
}

class MongoDbUserRepository @Inject()(db: DB) extends UserRepository {
  val collection: JSONCollection = db.collection[JSONCollection]("users")

  override def find(loginInfo: LoginInfo): Future[Option[User]] = {
    collection.find(Json.obj("loginInfo" -> loginInfo)).one[User]
  }

  override def save(user: User): Future[User] = {
    collection.insert(user)
    Future.successful(user)
  }

  override def find(userId: UUID): Future[Option[User]] = collection.find(Json.obj("userId" -> userId)).one[User]
}

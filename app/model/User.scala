package model

import java.util.UUID

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import play.api.libs.json.Json

case class User(userID: UUID, name: String, loginInfo: LoginInfo) extends Identity

object User {
  implicit val userJsonFormat = Json.format[User]
  implicit val loginInfoJsonFormat = Json.format[LoginInfo]
}
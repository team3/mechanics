package model

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Business(name: String, address: String, country: String, city: String, email: String, phone: Int)

object Business {


  def businessWrites(business: Business): JsValue = {
    JsObject(Seq(
      "name" -> JsString(business.name),
      "address" -> JsString(business.address),
      "country" -> JsString(business.country),
      "city" -> JsString(business.city),
      "email" -> JsString(business.email),
      "phone" -> JsNumber(business.phone)
    ))
  }

  implicit val businessReads: Reads[Business] = (
    (__ \ "name").read[String] ~
      (__ \ "address").read[String] ~
      (__ \ "country").read[String] ~
      (__ \ "city").read[String] ~
      (__ \ "email").read[String] ~
      (__ \ "phone").read[Int]
    ) (Business.apply _)


  object Fields {
    val Id = "_id"
    val Name = "name"
    val Address = "address"
    val Country = "country"
    val City = "city"
    val Email = "email"
    val Phone = "phone"
  }

}
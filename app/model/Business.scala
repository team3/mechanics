package model

import play.api.libs.json._

case class Business(name: String, address: String, country: String, city: String, email: String, phone: Int)

object Business {

  implicit object BusinessFormat extends Format[Business] {
    override def writes(business: Business): JsValue = {
      JsObject(Seq(
        "name" -> JsString(business.name),
        "address" -> JsString(business.address),
        "country" -> JsString(business.country),
        "city" -> JsString(business.city),
        "email" -> JsString(business.email),
        "phone" -> JsNumber(business.phone)
      ))
    }

    override def reads(json: JsValue): JsResult[Business] = {
      JsSuccess(Business(
        (json \ "name").as[String],
        (json \ "address").as[String],
        (json \ "country").as[String],
        (json \ "city").as[String],
        (json \ "email").as[String],
        (json \ "phone").as[Int]
      ))
    }
  }

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
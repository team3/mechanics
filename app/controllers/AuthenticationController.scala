package controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo, SignUpEvent, Silhouette}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import model.{LoginUser, User}
import modules.JWTEnv
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import service.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthenticationController @Inject()(val messagesApi: MessagesApi,
                                         silhouette: Silhouette[JWTEnv],
                                         userService: UserService,
                                         authInfoRepository: AuthInfoRepository,
                                         passwordHasher: PasswordHasher) extends Controller {

  implicit val signUpFormat = Json.format[LoginUser]

  def signUp = Action.async(parse.json) { implicit request =>
    request.body.validate[LoginUser].map { credentials =>
      val loginInfo = LoginInfo(CredentialsProvider.ID, credentials.email)

      userService.retrieve(loginInfo).flatMap {
        case Some(u) => Future.successful(Conflict(Json.obj("error" -> ("user " + credentials.email + " already registered"))))
        case None =>
          for {
            user <- userService.save(User(userID = UUID.randomUUID(), name = credentials.email, loginInfo))
            authInfo <- authInfoRepository.add(loginInfo, passwordHasher.hash(credentials.password))
            authenticator <- silhouette.env.authenticatorService.create(loginInfo)
            token <- silhouette.env.authenticatorService.init(authenticator)
            result <- silhouette.env.authenticatorService.embed(token, Ok(Json.toJson(token)))
          } yield {
            silhouette.env.eventBus.publish(SignUpEvent(user, request))
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            result
          }
      }
    }.recoverTotal {
      case error => Future.successful(BadRequest(Json.obj("error" -> "Invalid request")))
    }
  }
}
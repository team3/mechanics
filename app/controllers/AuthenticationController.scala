package controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.api.{Environment, LoginInfo, SignUpEvent, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import model.User
import modules.JWTEnv
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, Controller}
import service.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthenticationController @Inject()(val messagesApi: MessagesApi,
                                         silhouette: Silhouette[JWTEnv],
                                         userService: UserService,
                                         authInfoRepository: AuthInfoRepository,
                                         passwordHasher: PasswordHasher) extends Controller {


  def signUp = Action.async { implicit request =>

    // read user and password from configuration
    val email: String = "andr.parkhomenko1@gmail.com"
    val password: String = "password"

    val loginInfo = LoginInfo(CredentialsProvider.ID, email)

    userService.retrieve(loginInfo).flatMap {
      case Some(user) => Future.successful(Ok(user.loginInfo.providerID + " - " + user.loginInfo.providerKey))
      case None =>
        val authInfo = passwordHasher.hash(password)

        val user = User(userID = UUID.randomUUID(), email = email, loginInfo)

        for {
          user <- userService.save(user)
          authInfo <- authInfoRepository.add(loginInfo, authInfo)
          authenticator <- silhouette.env.authenticatorService.create(loginInfo)
          token <- silhouette.env.authenticatorService.init(authenticator)
          result <- silhouette.env.authenticatorService.embed(token, Ok(token))
        } yield {
          silhouette.env.eventBus.publish(SignUpEvent(user, request))
          result
        }
    }
  }
}
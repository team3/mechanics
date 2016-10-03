package controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.api.{Environment, LoginInfo, SignUpEvent, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import model.User
import play.api.i18n.MessagesApi
import play.api.mvc.Action
import service.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthenticationController @Inject()(val messagesApi: MessagesApi,
                                         val env: Environment[User, JWTAuthenticator],
                                         userService: UserService,
                                         authInfoRepository: AuthInfoRepository,
                                         passwordHasher: PasswordHasher) extends Silhouette[User, JWTAuthenticator] {

  def startSignUp = UserAwareAction.async { implicit request =>
    Future.successful(request.identity match {
      case Some(user) => Redirect(routes.Application.index())
      case None => Ok("Form")
    })
  }

  def signUp = Action.async { implicit request =>
    val email: String = "andr.parkhomenko@gmail.com"
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
          authenticator <- env.authenticatorService.create(loginInfo)
          value <- env.authenticatorService.init(authenticator)
          result <- env.authenticatorService.embed(value, Ok(user.loginInfo.toString))
        } yield {
          env.eventBus.publish(SignUpEvent(user, request, request2Messages))
          result
        }
    }
  }
}
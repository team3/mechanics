package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import model.User
import play.api.Configuration
import play.api.i18n.MessagesApi

import scala.concurrent.Future

class AuthenticationController @Inject()(val messagesApi: MessagesApi,
                                         val env: Environment[User, JWTAuthenticator],
                                         credentialsProvider: CredentialsProvider,
                                         configuration: Configuration) extends Silhouette[User, JWTAuthenticator] {

  def startSignUp = UserAwareAction.async { implicit request =>
    Future.successful(request.identity match {
      case Some(user) => Redirect(routes.Application.index())
      case None => Ok("Form")
    })
  }
}
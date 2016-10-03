import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.test.FakeEnvironment
import controllers.BusinessesController
import model.User
import modules.JWTEnv
import org.specs2.mock.Mockito
import play.api.test.{FakeHeaders, FakeRequest, PlaySpecification, WithApplication}
import com.mohiva.play.silhouette.test._

import scala.concurrent.ExecutionContext.Implicits.global

class BusinessControllerSpec extends PlaySpecification with Mockito {
  "The `save` method" should {
    "return status 401 if no authenticator was found" in new WithApplication {
      val identity = User(userID = UUID.randomUUID(), name = "test", loginInfo = LoginInfo("credentials", "test@gmail.com"))
      val env = FakeEnvironment[JWTEnv](Seq(identity.loginInfo -> identity))
      val request = FakeRequest()

      val controller = app.injector.instanceOf[BusinessesController]
      val result = controller.save("123")(request)

      status(result) must equalTo(UNAUTHORIZED)
    }
  }

  "The `save` method" should {
    "return status 200 if authenticator was found" in new WithApplication {
      val loginInfo = LoginInfo("credentials", "test@gmail.com")
      val identity = User(userID = UUID.randomUUID(), name = "test", loginInfo)
      implicit val env = FakeEnvironment[JWTEnv](Seq(identity.loginInfo -> identity))
      val request = FakeRequest().withAuthenticator[JWTEnv](identity.loginInfo)

      val controller = app.injector.instanceOf[BusinessesController]
      val result = controller.save("123")(request)

      status(result) must equalTo(OK)
    }
  }
}

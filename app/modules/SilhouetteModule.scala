package modules

import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services._
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus}
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.repositories.DelegableAuthInfoRepository
import com.mohiva.play.silhouette.impl.util._
import dao.{MongoDbUserRepository, PasswordInfoRepository, UserRepository}
import model.User
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.api._
import service.{UserService, UserServiceImpl}

class SilhouetteModule extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    bind[UserRepository].to[MongoDbUserRepository]
    bind[UserService].to[UserServiceImpl]
    bind[DB].toInstance {
      import com.typesafe.config.ConfigFactory

      import scala.collection.JavaConversions._
      import scala.concurrent.ExecutionContext.Implicits.global

      val config = ConfigFactory.load
      val driver = new MongoDriver
      val connection = driver.connection(
        config.getStringList("mongodb.servers"),
        MongoConnectionOptions(),
        Seq()
      )
      connection.db(config.getString("mongodb.db"))
    }
    bind[DelegableAuthInfoDAO[PasswordInfo]].to[PasswordInfoRepository]
    bind[CacheLayer].to[PlayCacheLayer]
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[PasswordHasher].toInstance(new BCryptPasswordHasher)
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())
  }

  @Provides def provideEnvironment(
                                    userService: UserService,
                                    authenticatorService: AuthenticatorService[JWTAuthenticator],
                                    eventBus: EventBus): Environment[User, JWTAuthenticator] = {
    Environment[User, JWTAuthenticator](userService, authenticatorService, Seq(), eventBus)
  }

  @Provides def provideAuthenticatorService(
                                             idGenerator: IDGenerator,
                                             configuration: Configuration,
                                             clock: Clock): AuthenticatorService[JWTAuthenticator] = {
    val config = configuration.underlying.as[JWTAuthenticatorSettings]("silhouette.authenticator")
    new JWTAuthenticatorService(config, None, idGenerator, clock)
  }

  @Provides
  def provideCredentialsProvider(
                                  authInfoRepository: AuthInfoRepository,
                                  passwordHasher: PasswordHasher): CredentialsProvider = {

    new CredentialsProvider(authInfoRepository, passwordHasher, Seq(passwordHasher))
  }

  @Provides def provideAuthInfoRepository(
                                           passwordInfoRepository: DelegableAuthInfoDAO[PasswordInfo]): AuthInfoRepository = {
    new DelegableAuthInfoRepository(passwordInfoRepository)
  }
}

package io.github.zidbrain.fchat.common.di

import io.github.zidbrain.Database
import io.github.zidbrain.fchat.common.account.cryptography.CryptographyService
import io.github.zidbrain.fchat.common.account.storage.EncryptedStorage
import io.github.zidbrain.fchat.common.account.storage.SsoStorage
import io.github.zidbrain.fchat.common.login.repository.LoginRepository
import io.ktor.client.HttpClient
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Qualifier
import kotlin.reflect.KClass
import kotlin.time.Clock

@Component
@AppScope
abstract class AppComponent(
    private val clientProvider: ClientProvider,
    @get:Provides val encryptedStorage: EncryptedStorage,
    @get:Provides val cryptographyService: CryptographyService,
    @get:Provides val ssoStorage: SsoStorage,
    @get:Provides val database: Database,
    @get:Provides val clock: Clock,
) {
    @AppScope
    @Provides
    @Authorized
    fun auth(loginRepository: LoginRepository): HttpClient = clientProvider.auth(loginRepository)

    @AppScope
    @Provides
    @Unauthorized
    fun unauth(): HttpClient = clientProvider.unauth()

    @get:AppScope
    @get:Provides
    @get:HostUrl
    val hostUrl: String = clientProvider.hostUrl
}

interface ClientProvider {
    fun auth(loginRepository: LoginRepository): HttpClient
    fun unauth(): HttpClient
    val hostUrl: String
}

@Qualifier
@Target(
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE
)
annotation class Authorized

@Qualifier
@Target(
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE
)
annotation class Unauthorized

@Qualifier
@Target(
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE
)
annotation class HostUrl

@Qualifier
@Target(
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE
)
annotation class Named(val type: KClass<*>)
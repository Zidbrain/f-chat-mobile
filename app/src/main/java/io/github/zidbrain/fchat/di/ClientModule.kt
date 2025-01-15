package io.github.zidbrain.fchat.di

import android.util.Log
import io.github.zidbrain.fchat.android.BuildConfig
import io.github.zidbrain.fchat.common.UnauthorizedException
import io.github.zidbrain.fchat.common.di.CommonQualifiers
import io.github.zidbrain.fchat.common.login.repository.LoginRepository
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.CIOEngineConfig
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.encodedPath
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Module
import org.koin.core.annotation.Qualifier
import org.koin.core.annotation.Single

@Module
class ClientModule {
    private fun createClient(setup: HttpClientConfig<CIOEngineConfig>.() -> Unit = {}) =
        HttpClient(CIO) {
            expectSuccess = true
            install(ContentNegotiation) {
                json()
            }
            WebSockets {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }
            Logging {
                level = LogLevel.ALL
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.i("HttpClient", message)
                    }
                }
            }
            defaultRequest {
                url.takeFrom(URLBuilder(BuildConfig.SERVER_URL).apply {
                    encodedPath += url.encodedPath
                })
            }
            HttpResponseValidator {
                handleResponseExceptionWithRequest { exception, request ->
                    val clientException = exception as? ClientRequestException
                        ?: return@handleResponseExceptionWithRequest
                    if (clientException.response.status == HttpStatusCode.Unauthorized)
                        throw UnauthorizedException(
                            message = "${request.method} ${request.url} returned 401 unauthorized",
                            cause = clientException
                        )
                }
            }
            setup()
        }

    @Single
    @Qualifier(CommonQualifiers.Unauthorized::class)
    fun unauth(): HttpClient = createClient()

    @Single
    @Qualifier(CommonQualifiers.Authorized::class)
    fun auth(loginRepository: LoginRepository): HttpClient = createClient {
        Auth {
            bearer {
                realm = "F Chat"
                loadTokens {
                    val state = loginRepository.authorizedSession
                    BearerTokens(state.accessToken, state.userSessionInfo.refreshToken)
                }
                refreshTokens {
                    try {
                        loginRepository.requestAccessToken()
                        val state = loginRepository.authorizedSession
                        BearerTokens(state.accessToken, state.userSessionInfo.refreshToken)
                    } catch (ex: Exception) {
                        Log.w(
                            "Access tokens",
                            "Error getting access token.\n${ex.stackTraceToString()}"
                        )
                        loginRepository.logout()
                        null
                    }
                }
            }
        }
    }

    @Single
    @Qualifier(CommonQualifiers.HostUrl::class)
    fun hostUrl(): String = BuildConfig.SERVER_URL
}
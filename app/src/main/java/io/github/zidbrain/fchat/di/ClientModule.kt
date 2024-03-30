package io.github.zidbrain.fchat.di

import io.github.zidbrain.fchat.android.BuildConfig
import io.github.zidbrain.fchat.common.login.repository.LoginRepository
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.CIOEngineConfig
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.URLBuilder
import io.ktor.http.encodedPath
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

private fun createClient(setup: HttpClientConfig<CIOEngineConfig>.() -> Unit = {}) =
    HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        Logging {
            level = LogLevel.INFO
            logger = Logger.ANDROID
        }
        defaultRequest {
            url.takeFrom(URLBuilder().takeFrom(BuildConfig.SERVER_URL).apply {
                encodedPath += url.encodedPath
            })
        }
        setup()
    }

val clientModule = module {
    single(qualifier(ClientType.Unauthorized)) {
        createClient()
    }
    single(qualifier(ClientType.Authorized)) {
        createClient {
            Auth {
                bearer {
                    realm = "F Chat"
                    val loginRepository = get<LoginRepository>()
                    loadTokens {
                        val state = loginRepository.authorizedSession
                        BearerTokens(state.accessToken, state.refreshToken)
                    }
                    refreshTokens {
                        try {
                            loginRepository.requestAccessToken()
                            val state = loginRepository.authorizedSession
                            BearerTokens(state.accessToken, state.refreshToken)
                        }
                        catch (ex: Exception) {
                            logger.warn("Error getting access token.\n${ex.stackTraceToString()}")
                            loginRepository.logout()
                            null
                        }
                    }
                }
            }
        }
    }
}

enum class ClientType {
    Unauthorized, Authorized
}
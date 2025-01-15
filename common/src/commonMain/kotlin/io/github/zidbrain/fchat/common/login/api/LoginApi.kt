package io.github.zidbrain.fchat.common.login.api

import io.github.zidbrain.fchat.common.di.CommonQualifiers
import io.github.zidbrain.fchat.common.login.api.dto.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.koin.core.annotation.Qualifier
import org.koin.core.annotation.Single

@Single
class LoginApi(
    @Qualifier(CommonQualifiers.Unauthorized::class)
    private val client: HttpClient
) {

    suspend fun getRefreshToken(request: GetRefreshTokenRequestDto): GetRefreshTokenResponseDto =
        client.post("/auth/getRefreshToken") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun getAccessToken(request: GetAccessTokenRequestDto): GetAccessTokenResponseDto =
        client.post("/auth/getAccessToken") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun signIn(request: SignInRequestDto): GetRefreshTokenResponseDto = client.post("/auth/signIn") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()
}
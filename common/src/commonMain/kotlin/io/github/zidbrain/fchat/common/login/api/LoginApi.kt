package io.github.zidbrain.fchat.common.login.api

import io.github.zidbrain.fchat.common.di.AppScope
import io.github.zidbrain.fchat.common.di.Unauthorized
import io.github.zidbrain.fchat.common.login.api.dto.GetAccessTokenRequestDto
import io.github.zidbrain.fchat.common.login.api.dto.GetAccessTokenResponseDto
import io.github.zidbrain.fchat.common.login.api.dto.GetRefreshTokenRequestDto
import io.github.zidbrain.fchat.common.login.api.dto.GetRefreshTokenResponseDto
import io.github.zidbrain.fchat.common.login.api.dto.SignInRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import me.tatarka.inject.annotations.Inject

@AppScope
@Inject
class LoginApi(
    @Unauthorized
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
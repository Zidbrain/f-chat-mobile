package io.github.zidbrain.fchat.common.fcm.api

import io.github.zidbrain.fchat.common.di.AppScope
import io.github.zidbrain.fchat.common.di.Authorized
import io.github.zidbrain.fchat.common.fcm.api.dto.UpdateFCMTokenRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.path
import me.tatarka.inject.annotations.Inject

@AppScope
@Inject
class FCMApi(@Authorized private val client: HttpClient) {

    suspend fun updateToken(request: UpdateFCMTokenRequest): Unit = client.post {
        url {
            path("/fcm/update-token")
        }
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()
}
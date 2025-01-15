package io.github.zidbrain.fchat.common.conversation.api

import io.github.zidbrain.fchat.common.conversation.api.dto.CreateConversationRequest
import io.github.zidbrain.fchat.common.conversation.api.dto.CreateConversationResponse
import io.github.zidbrain.fchat.common.conversation.api.dto.GetActiveDevicesRequest
import io.github.zidbrain.fchat.common.conversation.api.dto.GetActiveDevicesResponse
import io.github.zidbrain.fchat.common.conversation.api.dto.GetConversationInfoResponse
import io.github.zidbrain.fchat.common.di.CommonQualifiers
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.koin.core.annotation.Qualifier
import org.koin.core.annotation.Single

@Single
class ConversationApi(
    @Qualifier(CommonQualifiers.Authorized::class)
    private val client: HttpClient
) {
    suspend fun getActiveDevices(request: GetActiveDevicesRequest): GetActiveDevicesResponse =
        client.post("chat/getActiveDevices") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun createConversation(request: CreateConversationRequest): CreateConversationResponse =
        client.post("chat/createConversation") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun getConversationInfo(id: String): GetConversationInfoResponse? =
        client.get("chat/getConversationInfo/$id").let {
            if (it.status == HttpStatusCode.NotFound) null
            else it.body()
        }
}
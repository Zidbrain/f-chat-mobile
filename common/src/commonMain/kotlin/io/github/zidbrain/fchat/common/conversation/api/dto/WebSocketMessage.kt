@file:OptIn(ExperimentalSerializationApi::class)

package io.github.zidbrain.fchat.common.conversation.api.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@JsonClassDiscriminator("type")
@Serializable
sealed class WebSocketMessageOut {

    @Serializable
    @SerialName("request")
    data class Request(val payload: RequestPayload) : WebSocketMessageOut()

    @JsonClassDiscriminator("type")
    @Serializable
    sealed class RequestPayload {

        @Serializable
        @SerialName("message")
        data class Message(val message: String, val conversationId: String) : RequestPayload()
    }

    @Serializable
    @SerialName("ok")
    data object Ok : WebSocketMessageOut()
}

@JsonClassDiscriminator("type")
@Serializable
sealed class WebSocketMessageIn {

    @Serializable
    @SerialName("content")
    data class Content(val payload: ContentPayload) : WebSocketMessageIn()

    @Serializable
    @JsonClassDiscriminator("type")
    sealed class ContentPayload {

        @Serializable
        @SerialName("message")
        data class Message(
            val message: String,
            val senderId: String,
            val conversationId: String,
            val sentAt: Instant
        ) : ContentPayload()
    }

    @Serializable
    @SerialName("ok")
    data object Ok : WebSocketMessageIn()
}
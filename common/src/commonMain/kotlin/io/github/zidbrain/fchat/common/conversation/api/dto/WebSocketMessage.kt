@file:OptIn(ExperimentalSerializationApi::class)

package io.github.zidbrain.fchat.common.conversation.api.dto

import io.github.zidbrain.fchat.common.util.randomUUID
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
data class ChatSocketMessageOut(
    val socketMessageId: String = randomUUID(),
    val content: ChatSocketMessageOutContent
)

@JsonClassDiscriminator("type")
@Serializable
sealed class ChatSocketMessageOutContent {

    @JsonClassDiscriminator("type")
    @SerialName("payload")
    @Serializable
    sealed class Payload : ChatSocketMessageOutContent() {
        @Serializable
        @SerialName("messageRequest")
        data class CreateMessageRequest(
            val message: String,
            val conversationId: String,
        ) : Payload()
    }

    @JsonClassDiscriminator("type")
    @SerialName("control")
    @Serializable
    sealed class Control : ChatSocketMessageOutContent() {
        @Serializable
        @SerialName("ok")
        data object Ok : Control()
    }
}

@Serializable
@JsonClassDiscriminator("type")
sealed class ChatSocketMessageInContent {

    @JsonClassDiscriminator("type")
    @SerialName("payload")
    @Serializable
    sealed class Payload : ChatSocketMessageInContent() {
        @Serializable
        @SerialName("message")
        data class Message(
            val externalId: String,
            val message: String,
            val senderId: String,
            val conversationId: String,
            val sentAt: Instant,
        ) : Payload()
    }

    @JsonClassDiscriminator("type")
    @SerialName("control")
    @Serializable
    sealed class Control : ChatSocketMessageInContent() {
        @Serializable
        @SerialName("messageCreated")
        data class MessageCreated(val messageId: String) : Control()

        @Serializable
        @SerialName("ok")
        data object Ok : Control()

        @Serializable
        @SerialName("error")
        data class Error(val description: String) : Control()
    }
}

@Serializable
data class ChatSocketMessageIn(
    val socketMessageId: String,
    val content: ChatSocketMessageInContent
)
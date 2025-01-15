package io.github.zidbrain.fchat.common.conversation.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateConversationRequest(
    val members: List<ConversationMember>
) {

    @Serializable
    data class ConversationMember(val deviceId: String, val conversationEncryptedKey: String)
}

@Serializable
data class CreateConversationResponse(
    val conversationId: String
)
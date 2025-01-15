package io.github.zidbrain.fchat.common.conversation.api.dto

import io.github.zidbrain.fchat.common.chat.repository.Conversation
import io.github.zidbrain.fchat.common.contacts.api.dto.UserDto
import io.github.zidbrain.fchat.common.user.model.toModel
import io.ktor.util.decodeBase64Bytes
import kotlinx.serialization.Serializable

@Serializable
data class GetConversationInfoResponse(
    val id: String,
    val symmetricKey: String,
    val participants: List<UserDto>
) {
    fun toModel(): Conversation = Conversation(
        id = id,
        name = null,
        participants = participants.map { it.toModel() },
        symmetricKey = symmetricKey.decodeBase64Bytes(),
        messages = emptyList()
    )
}
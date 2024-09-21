package io.github.zidbrain.fchat.common.conversation.api.dto

import io.github.zidbrain.fchat.common.chat.repository.Conversation
import io.github.zidbrain.fchat.common.contacts.api.dto.UserDto
import io.github.zidbrain.fchat.common.user.model.toModel
import io.ktor.util.decodeBase64Bytes
import kotlinx.serialization.Serializable

@Serializable
data class GetConversationInfoResponse(
    val id: String,
    val encodedName: String?,
    val symmetricKey: String,
    val participants: List<UserDto>
) {
    fun toModel(decode: (String) -> String): Conversation = Conversation(
        id = id,
        name = encodedName?.let(decode),
        participants = participants.map { it.toModel() },
        symmetricKey = symmetricKey.decodeBase64Bytes(),
        messages = emptyList()
    )
}
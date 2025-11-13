package io.github.zidbrain.fchat.common.chat.model

import io.github.zidbrain.fchat.common.user.model.User

data class Conversation(
    val id: String,
    val name: String?,
    val participants: List<User>,
    val symmetricKey: ByteArray,
    val messages: List<ChatMessage>
) {
    fun toInfo(selfUserId: String): ConversationInfo {
        val type = if (participants.size == 2) {
            val (self, other) = if (participants.first().id == selfUserId)
                participants[0] to participants[1]
            else participants[1] to participants[0]
            ConversationType.Direct(self, other)
        } else {
            ConversationType.Group(participants, name!!)
        }
        return ConversationInfo(
            id = id,
            type = type,
            symmetricKey = symmetricKey,
            lastMessage = messages.lastOrNull()
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Conversation

        if (id != other.id) return false
        if (name != other.name) return false
        if (participants != other.participants) return false
        if (!symmetricKey.contentEquals(other.symmetricKey)) return false
        if (messages != other.messages) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + participants.hashCode()
        result = 31 * result + symmetricKey.contentHashCode()
        result = 31 * result + messages.hashCode()
        return result
    }
}

sealed interface ConversationType {
    data class Direct(val self: User, val other: User) : ConversationType
    data class Group(val participants: List<User>, val name: String) : ConversationType
}
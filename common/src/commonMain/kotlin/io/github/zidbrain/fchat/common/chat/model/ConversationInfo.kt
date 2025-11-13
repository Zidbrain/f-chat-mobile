package io.github.zidbrain.fchat.common.chat.model

data class ConversationInfo(
    val id: String,
    val type: ConversationType,
    val symmetricKey: ByteArray,
    val lastMessage: ChatMessage?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConversationInfo

        if (id != other.id) return false
        if (type != other.type) return false
        if (!symmetricKey.contentEquals(other.symmetricKey)) return false
        if (lastMessage != other.lastMessage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + symmetricKey.contentHashCode()
        result = 31 * result + (lastMessage?.hashCode() ?: 0)
        return result
    }
}
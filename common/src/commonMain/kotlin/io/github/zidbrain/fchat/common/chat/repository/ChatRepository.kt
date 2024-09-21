package io.github.zidbrain.fchat.common.chat.repository

import io.github.zidbrain.fchat.common.account.cryptography.AESKey
import io.github.zidbrain.fchat.common.account.cryptography.CryptographyService
import io.github.zidbrain.fchat.common.chat.api.ChatApi
import io.github.zidbrain.fchat.common.conversation.api.dto.WebSocketMessageIn
import io.github.zidbrain.fchat.common.conversation.api.dto.WebSocketMessageOut
import io.github.zidbrain.fchat.common.conversation.repository.ConversationRepository
import io.github.zidbrain.fchat.common.host.repository.SessionRepository
import io.github.zidbrain.fchat.common.user.model.User
import io.github.zidbrain.fchat.common.user.repository.UserRepository
import io.github.zidbrain.fchat.common.util.randomUUID
import io.ktor.util.decodeBase64Bytes
import kotlinx.datetime.Instant

class ChatRepository(
    private val chatApi: ChatApi,
    private val cryptographyService: CryptographyService,
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository,
    private val conversationRepository: ConversationRepository
) {

    private suspend fun message(message: WebSocketMessageIn.ContentPayload.Message) {
        val sender = userRepository.getUser(message.senderId)
        conversationRepository.addMessage(message.conversationId) {
            val key = AESKey(it.symmetricKey)
            val decryptedMessage = key.decrypt(message.message.decodeBase64Bytes()).decodeToString()
            ChatMessage(
                id = randomUUID(),
                sender = sender,
                content = decryptedMessage,
                status = MessageStatus.Delivered,
                sentAt = message.sentAt
            )
        }
    }

    suspend fun send(payload: WebSocketMessageOut.RequestPayload) {
        chatApi.send(payload)
    }

    suspend fun connect() {
        chatApi.connect { message ->
            when (message) {

                is WebSocketMessageIn.ContentPayload.Message -> message(message)
            }
        }
    }
}

data class ConversationInfo(
    val id: String,
    val name: String?,
    val participants: List<User>,
    val symmetricKey: ByteArray,
    val lastMessage: ChatMessage?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConversationInfo

        if (id != other.id) return false
        if (name != other.name) return false
        if (participants != other.participants) return false
        if (!symmetricKey.contentEquals(other.symmetricKey)) return false
        if (lastMessage != other.lastMessage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + participants.hashCode()
        result = 31 * result + symmetricKey.contentHashCode()
        result = 31 * result + (lastMessage?.hashCode() ?: 0)
        return result
    }
}

data class Conversation(
    val id: String,
    val name: String?,
    val participants: List<User>,
    val symmetricKey: ByteArray,
    val messages: List<ChatMessage>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Conversation

        if (id != other.id) return false
        if (participants != other.participants) return false
        if (!symmetricKey.contentEquals(other.symmetricKey)) return false
        if (messages != other.messages) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + participants.hashCode()
        result = 31 * result + symmetricKey.contentHashCode()
        result = 31 * result + messages.hashCode()
        return result
    }
}

enum class MessageStatus {
    Initial, Delivered, Read
}

data class ChatMessage(
    val id: String,
    val status: MessageStatus,
    val sender: User,
    val content: String,
    val sentAt: Instant
)
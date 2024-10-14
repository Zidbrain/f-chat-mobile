package io.github.zidbrain.fchat.common.chat.repository

import io.github.zidbrain.fchat.common.account.cryptography.AESKey
import io.github.zidbrain.fchat.common.chat.api.ChatApi
import io.github.zidbrain.fchat.common.conversation.api.dto.ChatSocketMessageInContent
import io.github.zidbrain.fchat.common.conversation.api.dto.ChatSocketMessageOutContent
import io.github.zidbrain.fchat.common.conversation.repository.ConversationRepository
import io.github.zidbrain.fchat.common.user.model.User
import io.github.zidbrain.fchat.common.user.repository.UserRepository
import io.github.zidbrain.fchat.common.util.randomUUID
import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64
import kotlinx.datetime.Instant
import org.koin.core.annotation.Single

@Single
class ChatRepository(
    private val chatApi: ChatApi,
    private val userRepository: UserRepository,
    private val conversationRepository: ConversationRepository
) {

    private suspend fun message(message: ChatSocketMessageInContent.Payload.Message) {
        val sender = userRepository.getUser(message.senderId)
        conversationRepository.addMessage(message.conversationId, message.externalId) {
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

    suspend fun sendNewMessage(conversationId: String, content: String) {
        val messageId = conversationRepository.createMessageIn(conversationId, content)

        try {
            val conversation = conversationRepository.getConversationInfo(conversationId)!!
            val key = AESKey(conversation.symmetricKey)
            val encodedMessage = key.encrypt(content.encodeToByteArray())

            val result = chatApi.send(
                ChatSocketMessageOutContent.Payload.CreateMessageRequest(
                    message = encodedMessage.encodeBase64(),
                    conversationId = conversationId
                )
            )
            result as? ChatSocketMessageInContent.Control.MessageCreated
                ?: throw IllegalStateException("Expected: ${ChatSocketMessageInContent.Control.MessageCreated}, got: $result")
            conversationRepository.setMessageExternalIdAndDeliveredStatus(
                messageId = messageId,
                externalMessageId = result.messageId
            )
        } catch (ex: Exception) {
            conversationRepository.setMessageStatus(messageId, MessageStatus.NotDelivered)
            throw ex
        }
    }

    /**
     * This function runs while the connection is active
     */
    suspend fun connect(onConnectionEstablished: () -> Unit): Nothing {
        chatApi.connect(onConnectionEstablished) { message ->
            when (message) {
                is ChatSocketMessageInContent.Payload.Message -> {
                    message(message)
                    ChatSocketMessageOutContent.Control.Ok
                }
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
    fun toInfo(): ConversationInfo = ConversationInfo(
        id = id,
        name = name,
        participants = participants,
        symmetricKey = symmetricKey,
        lastMessage = messages.lastOrNull()
    )

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

enum class MessageStatus {
    Initial, Delivered, Read, NotDelivered
}

data class ChatMessage(
    val id: String,
    val status: MessageStatus,
    val sender: User,
    val content: String,
    val sentAt: Instant
)
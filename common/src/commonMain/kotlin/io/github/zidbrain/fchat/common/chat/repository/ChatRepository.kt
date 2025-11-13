package io.github.zidbrain.fchat.common.chat.repository

import io.github.zidbrain.fchat.common.account.cryptography.AESKey
import io.github.zidbrain.fchat.common.chat.api.ChatApi
import io.github.zidbrain.fchat.common.chat.model.ChatMessage
import io.github.zidbrain.fchat.common.chat.model.MessageStatus
import io.github.zidbrain.fchat.common.conversation.api.dto.ChatSocketMessageInContent
import io.github.zidbrain.fchat.common.conversation.api.dto.ChatSocketMessageOutContent
import io.github.zidbrain.fchat.common.conversation.repository.ConversationRepository
import io.github.zidbrain.fchat.common.di.SessionScope
import io.github.zidbrain.fchat.common.user.repository.UserRepository
import io.github.zidbrain.fchat.common.util.randomUUID
import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64
import me.tatarka.inject.annotations.Inject

@Inject
@SessionScope
class ChatRepository(
    private val chatApi: ChatApi,
    private val userRepository: UserRepository,
    private val conversationRepository: ConversationRepository
) {

    suspend fun message(message: ChatSocketMessageInContent.Payload.Message): ChatMessage {
        val sender = userRepository.getUser(message.senderId)
        return conversationRepository.addMessage(message.conversationId, message.externalId) {
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
            conversationRepository.setMessageStatus(
                messageId,
                _root_ide_package_.io.github.zidbrain.fchat.common.chat.model.MessageStatus.NotDelivered
            )
            throw ex
        }
    }

    /**
     * This function runs while the connection is active
     */
    suspend fun connect(onConnectionEstablished: suspend () -> Unit): Nothing {
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

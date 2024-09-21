package io.github.zidbrain.fchat.common.conversation.repository

import io.github.zidbrain.fchat.common.account.cryptography.CryptographyService
import io.github.zidbrain.fchat.common.account.cryptography.encryptRSA
import io.github.zidbrain.fchat.common.chat.repository.ChatMessage
import io.github.zidbrain.fchat.common.chat.repository.Conversation
import io.github.zidbrain.fchat.common.chat.repository.ConversationInfo
import io.github.zidbrain.fchat.common.chat.repository.MessageStatus
import io.github.zidbrain.fchat.common.conversation.api.ConversationApi
import io.github.zidbrain.fchat.common.conversation.api.dto.CreateConversationRequest
import io.github.zidbrain.fchat.common.conversation.api.dto.GetActiveDevicesRequest
import io.github.zidbrain.fchat.common.conversation.local.ConversationDao
import io.github.zidbrain.fchat.common.host.repository.SessionRepository
import io.github.zidbrain.fchat.common.user.model.User
import io.github.zidbrain.fchat.common.user.repository.UserRepository
import io.github.zidbrain.fchat.common.util.randomUUID
import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

class ConversationRepository(
    private val api: ConversationApi,
    private val cryptographyService: CryptographyService,
    private val conversationDao: ConversationDao,
    private val repository: SessionRepository,
    private val userRepository: UserRepository
) {

    private var _conversations: Flow<Map<String, ConversationInfo>>? = null
    private val mutex = Mutex()

    suspend fun getConversations(): Flow<Map<String, ConversationInfo>> = mutex.withLock {
        _conversations ?: conversationDao.getAllConversations(repository.session.userId)
            .distinctUntilChanged()
            .map {
                it.associateBy { conv -> conv.id }
            }.also {
                _conversations = it
            }
    }

    suspend fun getConversation(conversationId: String): Flow<Conversation?> {
        val ownerId = repository.session.userId
        return conversationDao.getConversation(ownerId, conversationId)
    }

    suspend fun findDirectMessageConversation(withUserId: String): ConversationInfo? {
        val conversations = (_conversations ?: getConversations()).first()
        return conversations.values.find { it.participants.size == 1 && it.participants.first().id == withUserId }
    }

    /**
     * Requests all relevant info about [Conversation], creates it and saves it in the database.
     * @param users Participants of the conversation
     * @return Created [Conversation]
     */
    suspend fun createConversation(users: List<User>): Conversation {
        val usersId = users.map { it.id } +
                repository.session.userId
        val request = GetActiveDevicesRequest(usersId)
        val devices = api.getActiveDevices(request).devices
        val key = cryptographyService.generateSymmetricKey()
        val members = devices.map {
            val encryptedKey =
                key.encoded.encryptRSA(it.publicKey.decodeBase64Bytes()).encodeBase64()
            CreateConversationRequest.ConversationMember(it.id, encryptedKey)
        }

        val createConversationRequest = CreateConversationRequest(members)
        val conversation = api.createConversation(createConversationRequest)
        return conversationDao.createOrGetConversation(
            symmetricKey = key.encoded,
            conversationId = conversation.conversationId,
            ownerId = repository.session.userId,
            members = users,
            name = null
        )
    }

    /**
     * Adds message to conversation with [conversationId] to the local database.
     */
    suspend fun addMessage(conversationId: String, message: (ConversationInfo) -> ChatMessage) {
        val conversation = (_conversations ?: getConversations()).first()[conversationId]
            ?: throw IllegalStateException("No conversation in db. Must receive create conversation message first. TODO")
        conversationDao.addMessage(conversationId, message(conversation))
    }

    /**
     * Creates the message with specified [content] and adds it to conversation with [conversationId] in the local database.
     *
     * Assuming that sender is the current user.
     */
    suspend fun createMessageIn(conversationId: String, content: String) {
        val message = ChatMessage(
            id = randomUUID(),
            sentAt = Clock.System.now(),
            sender = userRepository.currentUser,
            content = content,
            status = MessageStatus.Initial
        )
        addMessage(conversationId) { message }
        // Testing
        GlobalScope.launch {
            delay(3000L)
            setMessageStatus(message.id, MessageStatus.Delivered)
            delay(3000L)
            setMessageStatus(message.id, MessageStatus.Read)
        }
    }

    private fun setMessageStatus(messageId: String, status: MessageStatus) {
        conversationDao.setMessageStatus(messageId, status)
    }

    fun clearHistoryForConversation(id: String) {
        conversationDao.clearMessagesForConversation(id)
    }
}
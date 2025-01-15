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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single

@Single
class ConversationRepository(
    private val api: ConversationApi,
    private val cryptographyService: CryptographyService,
    private val conversationDao: ConversationDao,
    private val repository: SessionRepository,
    private val userRepository: UserRepository,
    private val clock: Clock
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

    suspend fun getConversationInfo(conversationId: String): ConversationInfo? =
        getConversations().first()[conversationId]

    fun getConversation(conversationId: String): Flow<Conversation?> {
        val ownerId = repository.session.userId
        return conversationDao.getConversation(ownerId, conversationId)
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
        val currentDevicePublicKey = cryptographyService.deviceKeyPair(repository.session.email)
        return conversationDao.createOrGetConversation(
            encryptedSymmetricKey = currentDevicePublicKey.encrypt(key.encoded),
            conversationId = conversation.conversationId,
            ownerId = repository.session.userId,
            members = users + userRepository.currentUser,
            name = null
        )
    }

    /**
     * Adds message to conversation with [conversationId] to the local database.
     */
    suspend fun addMessage(
        conversationId: String,
        externalMessageId: String?,
        message: (ConversationInfo) -> ChatMessage
    ) {
        val conversation = getConversationInfo(conversationId)
            ?: requestConversationInfo(conversationId)
            ?: throw IllegalStateException("Cannot find conversation info for conversation id = $conversationId")
        conversationDao.addMessage(conversationId, externalMessageId, message(conversation))
    }

    private suspend fun requestConversationInfo(conversationId: String): ConversationInfo? {
        val response = api.getConversationInfo(conversationId)
        val conversation = response?.toModel() ?: return null

        val result = conversationDao.createOrGetConversation(
            encryptedSymmetricKey = conversation.symmetricKey,
            conversationId = conversation.id,
            ownerId = repository.session.userId,
            name = null,
            members = conversation.participants
        )
        return result.toInfo()
    }

    /**
     * Creates a message with the specified [content] and adds it to conversation with [conversationId] in the local database.
     *
     * Assuming that sender is the current user.
     *
     * @return Id of the new message
     */
    suspend fun createMessageIn(conversationId: String, content: String): String {
        val message = ChatMessage(
            id = randomUUID(),
            sentAt = clock.now(),
            sender = userRepository.currentUser,
            content = content,
            status = MessageStatus.Initial
        )
        addMessage(conversationId, null) { message }
        return message.id
    }

    fun setMessageExternalIdAndDeliveredStatus(messageId: String, externalMessageId: String) {
        conversationDao.setMessageExternalIdAndStatus(
            messageId,
            MessageStatus.Delivered,
            externalMessageId
        )
    }

    fun setMessageStatus(messageId: String, status: MessageStatus) {
        conversationDao.setMessageStatus(messageId, status)
    }

    fun clearHistoryForConversation(id: String) {
        conversationDao.clearMessagesForConversation(id)
    }
}
package io.github.zidbrain.fchat.common.conversation.local

import app.cash.sqldelight.coroutines.asFlow
import io.github.zidbrain.ConversationEntity
import io.github.zidbrain.ConversationParticipant
import io.github.zidbrain.ConversationWithMessages
import io.github.zidbrain.Database
import io.github.zidbrain.MessageEntity
import io.github.zidbrain.ParticipantEntity
import io.github.zidbrain.fchat.common.chat.repository.ChatMessage
import io.github.zidbrain.fchat.common.chat.repository.Conversation
import io.github.zidbrain.fchat.common.chat.repository.ConversationInfo
import io.github.zidbrain.fchat.common.chat.repository.MessageStatus
import io.github.zidbrain.fchat.common.user.model.User
import io.github.zidbrain.fchat.common.user.repository.UserRepository
import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.Instant

class ConversationDao(private val database: Database, private val userRepository: UserRepository) {

    private data class ConversationDBModel(
        val id: String,
        val symmetricKey: String,
        val name: String?
    )

    private suspend fun List<ConversationWithMessages>.toModel(): List<Conversation> =
        coroutineScope {
            groupBy {
                ConversationDBModel(
                    id = it.id,
                    symmetricKey = it.symmetricKey,
                    name = it.conversationName
                )
            }.map { (model, it) ->
                val participants = it.map { participant ->
                    async { userRepository.getUser(participant.participantId) }
                }
                val messages = it.mapNotNull { message ->
                    message.messageId?.let {
                        async {
                            ChatMessage(
                                id = it,
                                sender = userRepository.getUser(message.messageSentBy!!),
                                content = message.messageContent!!,
                                sentAt = Instant.fromEpochMilliseconds(message.messageSentAt!!),
                                status = message.messageStatus!!
                            )
                        }
                    }
                }

                Conversation(
                    id = model.id,
                    name = model.name,
                    participants = participants.awaitAll(),
                    symmetricKey = model.symmetricKey.decodeBase64Bytes(),
                    messages = messages.awaitAll()
                )
            }
        }

    suspend fun getAllConversations(userId: String): Flow<List<ConversationInfo>> {
        val list = database.conversationsQueries.getAllConversations(userId).asFlow()
        return list.map { query ->
            query.executeAsList().toModel().map {
                ConversationInfo(
                    id = it.id,
                    name = it.name,
                    participants = it.participants,
                    symmetricKey = it.symmetricKey,
                    lastMessage = it.messages.singleOrNull()
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getConversation(ownerId: String, id: String): Flow<Conversation?> =
        database.conversationsQueries.getConversationById(
            ownerId = ownerId,
            conversationId = id
        ).asFlow()
            .distinctUntilChanged()
            .mapLatest {
                it.executeAsList().toModel().singleOrNull()
            }

    suspend fun createOrGetConversation(
        symmetricKey: ByteArray,
        conversationId: String,
        ownerId: String,
        name: String?,
        members: List<User>
    ): Conversation = with(database.conversationsQueries) {
        val existing = getConversationById(ownerId, conversationId).executeAsList()
        if (existing.isNotEmpty()) {
            members.forEach { user ->
                createParticipant(ParticipantEntity(user.id))
                addParticipantToConversation(ConversationParticipant(conversationId, user.id))
            }
            return existing.toModel().first()
        }

        createConversation(
            ConversationEntity(
                conversationId,
                ownerId,
                symmetricKey.encodeBase64(),
                name
            )
        )
        members.forEach { user ->
            createParticipant(ParticipantEntity(user.id))
            addParticipantToConversation(ConversationParticipant(conversationId, user.id))
        }
        return Conversation(
            id = conversationId,
            participants = members,
            symmetricKey = symmetricKey,
            name = name,
            messages = emptyList()
        )
    }

    fun addMessage(conversationId: String, message: ChatMessage) =
        with(database.conversationsQueries) {
            addMessage(
                MessageEntity(
                    id = message.id,
                    content = message.content,
                    conversationId = conversationId,
                    sentAt = message.sentAt.toEpochMilliseconds(),
                    sentBy = message.sender.id,
                    status = message.status
                )
            )
        }

    fun clearMessagesForConversation(id: String) {
        database.conversationsQueries.clearMessagesForConversation(id)
    }

    fun setMessageStatus(messageId: String, status: MessageStatus) {
        database.conversationsQueries.setMessageStatus(status = status, id = messageId)
    }
}
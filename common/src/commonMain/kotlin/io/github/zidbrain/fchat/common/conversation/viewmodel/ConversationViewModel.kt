package io.github.zidbrain.fchat.common.conversation.viewmodel

import io.github.zidbrain.fchat.common.chat.repository.Conversation
import io.github.zidbrain.fchat.common.chat.repository.MessageStatus
import io.github.zidbrain.fchat.common.conversation.repository.ConversationRepository
import io.github.zidbrain.fchat.common.nav.ConversationNavigationInfo
import io.github.zidbrain.fchat.common.user.repository.UserRepository
import io.github.zidbrain.fchat.mvi.MVIActionBuilder
import io.github.zidbrain.fchat.mvi.MVIViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ConversationViewModel(
    private var navInfo: ConversationNavigationInfo,
    private val conversationRepository: ConversationRepository,
    private val userRepository: UserRepository,
) : MVIViewModel<ConversationAction, ConversationState, Nothing>(ConversationState.Loading) {

    private var conversationId: String? = null

    private fun Conversation.toState(): ConversationState.Content {
        val currentTimeZone = TimeZone.currentSystemDefault()
        val currentUserId = userRepository.currentUser.id
        return ConversationState.Content(
            messagesByDate = messages
                .map {
                    ConversationState.ConversationMessage(
                        id = it.id,
                        senderName = it.sender.name,
                        isSenderSelf = it.sender.id == currentUserId,
                        senderIconUrl = "icon",
                        content = it.content,
                        sentAt = it.sentAt,
                        status = it.status
                    )
                }
                .groupBy { it.sentAt.toLocalDateTime(currentTimeZone).date }
                .map { (date, messages) -> ConversationState.MessagesByDate(date, messages) },
            iconUrl = "icon",
            name = name ?: participants.first().name
        )
    }

    override val initAction: MVIActionBuilder<ConversationState, Nothing> = buildAction {
        navInfo.let {
            when (it) {
                is ConversationNavigationInfo.ConversationId -> {
                    conversationId = it.id
                    val flow = conversationRepository.getConversation(it.id)
                        .filterNotNull()
                        .map { conversation -> conversation.toState() }
                    setStateBy(flow)
                }

                is ConversationNavigationInfo.NewDirectMessageConversation -> {
                    val user = userRepository.getUser(it.withUserId)
                    setState(
                        ConversationState.Content(
                            messagesByDate = emptyList(),
                            iconUrl = "icon",
                            name = user.name
                        )
                    )
                }
            }
        }
    }

    override fun handleAction(action: ConversationAction): MVIActionBuilder<ConversationState, Nothing> =
        buildAction {
            when (action) {
                is ConversationAction.Send -> navInfo.let {
                    val convId = when (it) {
                        is ConversationNavigationInfo.ConversationId -> it.id

                        is ConversationNavigationInfo.NewDirectMessageConversation -> {
                            val user = userRepository.getUser(it.withUserId)
                            val conversation =
                                conversationRepository.createConversation(listOf(user))
                            conversationId = conversation.id
                            navInfo = ConversationNavigationInfo.ConversationId(conversation.id)
                            sendInitAction()
                            conversation.id
                        }
                    }
                    val message = action.message.trimEnd { mes -> mes.isISOControl() }
                    conversationRepository.createMessageIn(convId, message)
                }

                ConversationAction.ClearHistory -> conversationId?.let {
                    conversationRepository.clearHistoryForConversation(it)
                }
            }
        }
}

sealed class ConversationAction {
    data class Send(val message: String) : ConversationAction()
    data object ClearHistory : ConversationAction()
}

sealed class ConversationState {
    data object Loading : ConversationState()
    data class Content(
        val messagesByDate: List<MessagesByDate>,
        val iconUrl: String,
        val name: String
    ) : ConversationState()

    data class MessagesByDate(
        val date: LocalDate,
        val messages: List<ConversationMessage>
    )

    data class ConversationMessage(
        val id: String,
        val status: MessageStatus,
        val senderName: String,
        val isSenderSelf: Boolean,
        val senderIconUrl: String,
        val content: String,
        val sentAt: Instant
    )
}
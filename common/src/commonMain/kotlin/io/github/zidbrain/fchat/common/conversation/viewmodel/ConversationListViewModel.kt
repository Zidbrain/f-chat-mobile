package io.github.zidbrain.fchat.common.conversation.viewmodel

import io.github.zidbrain.fchat.common.conversation.repository.ConversationRepository
import io.github.zidbrain.fchat.common.host.repository.SessionRepository
import io.github.zidbrain.fchat.mvi.MVIViewModel
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class ConversationListViewModel(
    private val conversationRepository: ConversationRepository,
    private val sessionRepository: SessionRepository
) :
    MVIViewModel<ConversationListAction, ConversationListState, ConversationListEvent>(
        ConversationListState.Default
    ) {

    override val initAction = buildAction {
        setState(ConversationListState.Loading)

        setStateBy(conversationRepository.getConversations().map { conversation ->
            ConversationListState(
                conversations = conversation.values.map {
                    val displayedName =
                        if (it.participants.size == 1) it.participants.first().name else it.name
                    ConversationListState.ConversationOverviewModel(
                        id = it.id,
                        displayedName = displayedName!!,
                        lastMessage = it.lastMessage?.let { message ->
                            val sentBySelf = sessionRepository.session.userId == message.sender.id
                            ConversationListState.ChatMessageOverviewModel(
                                sentBySelf = sentBySelf,
                                content = message.content,
                                sentAt = message.sentAt
                            )
                        }
                    )
                },
                loading = ConversationListState.LoadingState.Complete
            )
        })
    }.catch {
        setState { copy(loading = ConversationListState.LoadingState.Error) }
    }

    override fun handleAction(action: ConversationListAction) = buildAction {
        TODO("Not yet implemented")
    }
}

sealed class ConversationListAction

data class ConversationListState(
    val conversations: List<ConversationOverviewModel>,
    val loading: LoadingState
) {

    enum class LoadingState {
        Complete,
        Loading,
        Error
    }

    data class ConversationOverviewModel(
        val id: String,
        val displayedName: String,
        val lastMessage: ChatMessageOverviewModel?
    )

    data class ChatMessageOverviewModel(
        val sentBySelf: Boolean,
        val content: String,
        val sentAt: Instant
    )

    companion object {
        val Default = ConversationListState(
            conversations = emptyList(),
            loading = LoadingState.Complete
        )
        val Loading = ConversationListState(
            conversations = emptyList(),
            loading = LoadingState.Loading
        )
    }
}

sealed class ConversationListEvent
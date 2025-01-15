package io.github.zidbrain.fchat.common.chat.viewmodel

import io.github.zidbrain.fchat.common.chat.repository.ChatRepository
import io.github.zidbrain.fchat.common.user.model.User
import io.github.zidbrain.fchat.common.user.repository.UserRepository
import io.github.zidbrain.fchat.mvi.MVIActionBuilder
import io.github.zidbrain.fchat.mvi.MVIViewModel
import kotlinx.coroutines.delay
import org.koin.android.annotation.KoinViewModel
import kotlin.time.Duration.Companion.seconds

@KoinViewModel
class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) :
    MVIViewModel<ChatAction, ChatState, Nothing>(
        ChatState.Loading
    ) {

    private fun restoreConnection(): MVIActionBuilder<ChatState, Nothing> = buildAction {
        setState(ChatState.Loading)
        chatRepository.connect {
            val userInfo = userRepository.currentUser
            setState(ChatState.Connected(userInfo))
        }
    }.retry {
        setState(ChatState.Error(it))
        delay(5.seconds)
        true
    }

    private fun pauseConnection(): MVIActionBuilder<ChatState, Nothing> = buildAction {
        cancelAction(ChatAction.RestoreConnection::class)
    }

    override fun handleAction(action: ChatAction): MVIActionBuilder<ChatState, Nothing> =
        when (action) {
            ChatAction.PauseConnection -> pauseConnection()
            ChatAction.RestoreConnection -> restoreConnection()
        }
}

sealed class ChatAction {
    data object PauseConnection : ChatAction()
    data object RestoreConnection : ChatAction()
}

sealed class ChatState {
    data object Loading : ChatState()
    data class Connected(val user: User) : ChatState()
    data class Error(val ex: Exception) : ChatState()
}
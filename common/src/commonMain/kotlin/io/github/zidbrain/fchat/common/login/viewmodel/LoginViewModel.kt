package io.github.zidbrain.fchat.common.login.viewmodel

import io.github.zidbrain.fchat.common.UnauthorizedException
import io.github.zidbrain.fchat.common.host.repository.SessionRepository
import io.github.zidbrain.fchat.common.host.repository.UserSessionState
import io.github.zidbrain.fchat.common.login.repository.LoginRepository
import io.github.zidbrain.fchat.mvi.MVIViewModel
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class LoginViewModel(
    private val loginRepository: LoginRepository,
    private val sessionRepository: SessionRepository
) :
    MVIViewModel<LoginAction, LoginState, Nothing>(LoginState.Empty) {

    override val initAction = buildAction {
        if (sessionRepository.state.value is UserSessionState.Empty) {
            setState(LoginState.Content(false))
            return@buildAction
        }

        setState(LoginState.Content(true))
        try {
            loginRepository.requestAccessToken()
        } catch (ex: UnauthorizedException) {
            loginRepository.logout()
        }
        setState(LoginState.Content(false))
    }.onErrorSet(LoginState::Error)

    override fun handleAction(action: LoginAction) = buildAction {
        when (action) {
            is LoginAction.Login -> {
                setState(LoginState.Content(true))
                loginRepository.login(action.idToken, action.email)
                setState(LoginState.Content(false))
            }
        }
    }.onErrorSet(LoginState::Error)
}

sealed class LoginAction {
    data class Login(val idToken: String, val email: String) : LoginAction()
}

sealed class LoginState {
    data object Empty : LoginState()
    data class Content(val loading: Boolean) : LoginState()
    data class Error(val cause: Throwable) : LoginState()
}
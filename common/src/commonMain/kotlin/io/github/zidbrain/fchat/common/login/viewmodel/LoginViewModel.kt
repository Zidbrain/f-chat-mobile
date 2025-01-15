package io.github.zidbrain.fchat.common.login.viewmodel

import androidx.compose.runtime.Immutable
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
            setState(LoginState.Buttons())
            return@buildAction
        }

        setStateBy(handleAction(LoginAction.TryAuth))
    }.onErrorSet(LoginState::Buttons)

    override fun handleAction(action: LoginAction) = buildAction {
        when (action) {
            is LoginAction.GoogleLogin -> {
                setState(LoginState.Loading)
                loginRepository.loginWithGoogle(action.idToken, action.email)
                setState(LoginState.Buttons())
            }

            LoginAction.ToEmailInput -> setState(LoginState.EmailInput)
            LoginAction.ToButtons -> setState(LoginState.Buttons())
            LoginAction.TryAuth -> {
                setState(LoginState.Loading)
                try {
                    loginRepository.requestAccessToken()
                } catch (ex: UnauthorizedException) {
                    loginRepository.logout()
                }
                setState(LoginState.Buttons())
            }
        }
    }.onErrorSet(LoginState::Buttons)
}

sealed class LoginAction {
    data class GoogleLogin(val idToken: String, val email: String) : LoginAction()
    data object ToEmailInput : LoginAction()
    data object ToButtons : LoginAction()
    data object TryAuth : LoginAction()
}

sealed class LoginState {
    @Immutable
    data object Empty : LoginState()

    @Immutable
    data class Buttons(val error: Throwable? = null) : LoginState()

    @Immutable
    data object EmailInput : LoginState()

    @Immutable
    data object Loading : LoginState()
}
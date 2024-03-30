package io.github.zidbrain.fchat.common.login.viewmodel

import io.github.zidbrain.fchat.common.login.repository.LoginRepository
import io.github.zidbrain.fchat.mvi.MVIViewModel
import kotlinx.coroutines.flow.flow

class LoginViewModel(private val loginRepository: LoginRepository) :
    MVIViewModel<LoginAction, Unit, Nothing>(Unit) {

    override fun handleAction(action: LoginAction) = flow<Unit> {
        when (action) {
            is LoginAction.Login -> {
                loginRepository.login(action.idToken, action.email)
            }
        }
    }
}

sealed class LoginAction {
    data class Login(val idToken: String, val email: String) : LoginAction()
}
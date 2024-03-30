package io.github.zidbrain.fchat.common.host.viewmodel

import io.github.zidbrain.fchat.common.host.repository.SessionRepository
import io.github.zidbrain.fchat.common.host.repository.UserSessionState
import io.github.zidbrain.fchat.common.login.repository.LoginRepository
import io.github.zidbrain.fchat.mvi.MVIViewModel
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull

class HostViewModel(
    private val sessionRepository: SessionRepository,
    private val loginRepository: LoginRepository
) :
    MVIViewModel.Actionless<HostState>(HostState.Loading) {

    override fun onInit() = flow {
        emitAll(
            sessionRepository.state.mapNotNull {
                when (it) {
                    UserSessionState.Empty -> HostState.LogIn
                    is UserSessionState.ActiveSession.Unauthorized -> {
                        try {
                            loginRepository.requestAccessToken()
                        } catch (ex: Exception) {
                            loginRepository.logout()
                        }
                        null
                    }

                    is UserSessionState.ActiveSession.Authorized -> HostState.Main
                }
            }.errorState(HostState::Error)
        )
    }
}

sealed class HostState {
    data object Loading : HostState()
    data object LogIn : HostState()
    data object Main : HostState()
    data class Error(val error: Throwable) : HostState()
}
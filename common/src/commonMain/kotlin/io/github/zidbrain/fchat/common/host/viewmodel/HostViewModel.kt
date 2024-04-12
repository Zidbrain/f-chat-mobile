package io.github.zidbrain.fchat.common.host.viewmodel

import io.github.zidbrain.fchat.common.host.repository.SessionRepository
import io.github.zidbrain.fchat.common.host.repository.UserSessionState
import io.github.zidbrain.fchat.mvi.MVIViewModel
import kotlinx.coroutines.flow.map

class HostViewModel(
    private val sessionRepository: SessionRepository
) :
    MVIViewModel.Actionless<HostState>(HostState.LogIn) {

    override val initAction = buildAction {
        setStateBy(
            sessionRepository.state.map {
                when (it) {
                    UserSessionState.Empty, is UserSessionState.ActiveSession.Unauthorized -> HostState.LogIn
                    is UserSessionState.ActiveSession.Authorized -> HostState.Main
                }
            }
        )
    }.onErrorSet(HostState::Error)
}

sealed class HostState {
    data object LogIn : HostState()
    data object Main : HostState()
    data class Error(val error: Throwable) : HostState()
}
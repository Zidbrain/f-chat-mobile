package io.github.zidbrain.fchat.common.host.repository

import io.github.zidbrain.fchat.common.account.storage.EncryptedStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SessionRepository(
    private val encryptedStorage: EncryptedStorage
) {
    private fun getUserState(): UserSessionState =
        encryptedStorage.refreshToken?.let { UserSessionState.ActiveSession.Unauthorized(it) }
            ?: UserSessionState.Empty

    private val _state = MutableStateFlow(getUserState())
    val state = _state.asStateFlow()

    val session: UserSessionState.ActiveSession
        get() = _state.value as UserSessionState.ActiveSession

    fun createSession(refreshToken: String) {
        _state.update {
            UserSessionState.ActiveSession.Unauthorized(refreshToken)
        }
        encryptedStorage.refreshToken = refreshToken
    }

    fun authorize(accessToken: String) {
        val refreshToken = session.refreshToken
        _state.update {
            UserSessionState.ActiveSession.Authorized(accessToken, refreshToken)
        }
    }

    fun logout() {
        _state.update {
            UserSessionState.Empty
        }
        encryptedStorage.refreshToken = null
    }
}

sealed class UserSessionState {
    data object Empty : UserSessionState()
    sealed class ActiveSession : UserSessionState() {
        abstract val refreshToken: String

        data class Unauthorized(override val refreshToken: String) : ActiveSession()
        data class Authorized(val accessToken: String, override val refreshToken: String) :
            ActiveSession()
    }
}
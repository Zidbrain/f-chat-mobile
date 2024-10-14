package io.github.zidbrain.fchat.common.host.repository

import io.github.zidbrain.fchat.common.account.storage.EncryptedStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class SessionRepository(
    private val encryptedStorage: EncryptedStorage
) {
    private fun getUserState(): UserSessionState {
        val sessionInfo = encryptedStorage.userSession
        return if (sessionInfo == null)
            UserSessionState.Empty
        else UserSessionState.ActiveSession.Unauthorized(sessionInfo)
    }

    private val _state = MutableStateFlow(getUserState())
    val state = _state.asStateFlow()

    val session: UserSessionInfo
        get() = (_state.value as UserSessionState.ActiveSession).userSessionInfo

    val accessToken: String
        get() = (_state.value as UserSessionState.ActiveSession.Authorized).accessToken

    fun createSession(refreshToken: String, userId: String, email: String) {
        val session = UserSessionInfo(refreshToken, userId, email)
        _state.update {
            UserSessionState.ActiveSession.Unauthorized(session)
        }
        encryptedStorage.userSession = session
    }

    fun authorize(accessToken: String) {
        _state.update {
            UserSessionState.ActiveSession.Authorized(accessToken, session)
        }
    }

    fun logout() {
        _state.update {
            UserSessionState.Empty
        }
        encryptedStorage.userSession = null
    }
}

data class UserSessionInfo(
    val refreshToken: String,
    val userId: String,
    val email: String
)

sealed class UserSessionState {
    data object Empty : UserSessionState()
    sealed class ActiveSession : UserSessionState() {
        abstract val userSessionInfo: UserSessionInfo

        data class Unauthorized(
            override val userSessionInfo: UserSessionInfo
        ) : ActiveSession()

        data class Authorized(
            val accessToken: String,
            override val userSessionInfo: UserSessionInfo
        ) : ActiveSession()
    }
}
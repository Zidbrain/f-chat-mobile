package io.github.zidbrain.fchat.common.host.repository

import io.github.zidbrain.fchat.common.account.encryption.EncryptionService
import io.github.zidbrain.fchat.common.account.storage.EncryptedStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SessionRepository(
    private val encryptedStorage: EncryptedStorage,
    private val encryptionService: EncryptionService
) {
    private fun getUserState(): UserSessionState {
        val refreshToken = encryptedStorage.refreshToken
        val email = encryptedStorage.email
        if (refreshToken == null || email == null)
            return UserSessionState.Empty

        val devicePublicKey = encryptionService.devicePublicKey(email)
        return UserSessionState.ActiveSession.Unauthorized(
            UserSessionInfo(
                refreshToken = refreshToken,
                email = email,
                devicePublicKey = devicePublicKey
            )
        )
    }

    private val _state = MutableStateFlow(getUserState())
    val state = _state.asStateFlow()

    val session: UserSessionInfo
        get() = (_state.value as UserSessionState.ActiveSession).userSessionInfo

    fun createSession(refreshToken: String, devicePublicKey: String, email: String) {
        _state.update {
            UserSessionState.ActiveSession.Unauthorized(
                UserSessionInfo(
                    refreshToken = refreshToken,
                    email = email,
                    devicePublicKey = devicePublicKey
                )
            )
        }
        encryptedStorage.email = email
        encryptedStorage.refreshToken = refreshToken
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
        encryptedStorage.refreshToken = null
        encryptedStorage.email = null
    }
}

data class UserSessionInfo(
    val refreshToken: String,
    val email: String,
    val devicePublicKey: String
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
package io.github.zidbrain.fchat.common.login.repository

import io.github.zidbrain.fchat.common.account.encryption.EncryptionService
import io.github.zidbrain.fchat.common.account.storage.SsoStorage
import io.github.zidbrain.fchat.common.host.repository.SessionRepository
import io.github.zidbrain.fchat.common.host.repository.UserSessionState
import io.github.zidbrain.fchat.common.login.api.LoginApi
import io.github.zidbrain.fchat.common.login.api.dto.GetAccessTokenRequestDto
import io.github.zidbrain.fchat.common.login.api.dto.GetRefreshTokenRequestDto

class LoginRepository(
    private val loginApi: LoginApi,
    private val encryptionService: EncryptionService,
    private val sessionRepository: SessionRepository,
    private val ssoStorage: SsoStorage
) {

    val authorizedSession
        get() = sessionRepository.state.value as UserSessionState.ActiveSession.Authorized

    suspend fun login(idToken: String, email: String) {
        val publicKey = encryptionService.devicePublicKey(email)
        val request = GetRefreshTokenRequestDto(
            idToken = idToken,
            devicePublicKey = publicKey
        )
        val response = loginApi.getRefreshToken(request)
        sessionRepository.createSession(response.refreshToken)

        requestAccessToken()
    }

    suspend fun requestAccessToken() {
        val refreshToken = sessionRepository.session.refreshToken
        val request = GetAccessTokenRequestDto(
            refreshToken = refreshToken
        )
        val response = loginApi.getAccessToken(request)
        sessionRepository.authorize(response.accessToken)
    }

    suspend fun logout() {
        sessionRepository.logout()
        ssoStorage.clear()
    }
}
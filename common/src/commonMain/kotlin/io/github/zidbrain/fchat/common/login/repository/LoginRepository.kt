package io.github.zidbrain.fchat.common.login.repository

import io.github.zidbrain.fchat.common.account.cryptography.CryptographyService
import io.github.zidbrain.fchat.common.account.storage.SsoStorage
import io.github.zidbrain.fchat.common.host.repository.SessionRepository
import io.github.zidbrain.fchat.common.host.repository.UserSessionState
import io.github.zidbrain.fchat.common.login.api.LoginApi
import io.github.zidbrain.fchat.common.login.api.dto.GetAccessTokenRequestDto
import io.github.zidbrain.fchat.common.login.api.dto.GetRefreshTokenRequestDto
import io.github.zidbrain.fchat.common.login.api.dto.GetRefreshTokenRequestDtoData
import io.github.zidbrain.fchat.common.login.api.dto.SignInRequestDto
import io.ktor.util.encodeBase64
import org.koin.core.annotation.Single

@Single
class LoginRepository(
    private val loginApi: LoginApi,
    private val cryptographyService: CryptographyService,
    private val sessionRepository: SessionRepository,
    private val ssoStorage: SsoStorage
) {

    val authorizedSession
        get() = sessionRepository.state.value as UserSessionState.ActiveSession.Authorized

    suspend fun loginWithGoogle(idToken: String, email: String) {
        val publicKey = cryptographyService.deviceKeyPair(email).public.encodeBase64()
        val request = GetRefreshTokenRequestDto(
            data = GetRefreshTokenRequestDtoData.GoogleSSO(
                idToken = idToken,
                devicePublicKey = publicKey
            )
        )
        val response = loginApi.getRefreshToken(request)
        sessionRepository.createSession(response.refreshToken, response.userId, email)

        requestAccessToken()
    }

    suspend fun loginWithEmail(email: String, password: String) {
        val publicKey = cryptographyService.deviceKeyPair(email).public.encodeBase64()
        val request = GetRefreshTokenRequestDto(
            GetRefreshTokenRequestDtoData.PasswordAuth(
                email = email,
                password = password,
                devicePublicKey = publicKey
            )
        )

        val response = loginApi.getRefreshToken(request)
        sessionRepository.createSession(response.refreshToken, response.userId, email)

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

    suspend fun signIn(email: String, password: String) {
        val publicKey = cryptographyService.deviceKeyPair(email).public.encodeBase64()
        val request = SignInRequestDto(
            email = email,
            password = password,
            devicePublicKey = publicKey
        )
        val response = loginApi.signIn(request)
        sessionRepository.createSession(response.refreshToken, response.userId, email)

        requestAccessToken()
    }

    suspend fun logout() {
        sessionRepository.logout()
        ssoStorage.clear()
    }
}
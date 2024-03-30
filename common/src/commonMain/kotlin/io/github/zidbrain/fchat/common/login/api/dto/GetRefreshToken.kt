package io.github.zidbrain.fchat.common.login.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class GetRefreshTokenRequestDto(val idToken: String, val devicePublicKey: String)

@Serializable
data class GetRefreshTokenResponseDto(val refreshToken: String)
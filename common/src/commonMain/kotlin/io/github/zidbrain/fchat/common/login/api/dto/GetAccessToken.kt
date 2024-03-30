package io.github.zidbrain.fchat.common.login.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class GetAccessTokenRequestDto(val refreshToken: String)

@Serializable
data class GetAccessTokenResponseDto(val accessToken: String)
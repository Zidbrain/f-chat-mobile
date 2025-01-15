package io.github.zidbrain.fchat.common.login.api.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
data class GetRefreshTokenRequestDto(val data: GetRefreshTokenRequestDtoData)

@Serializable
data class GetRefreshTokenResponseDto(val refreshToken: String, val userId: String)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("authType")
sealed class GetRefreshTokenRequestDtoData {

    abstract val devicePublicKey: String

    @Serializable
    @SerialName("googleSSO")
    data class GoogleSSO(val idToken: String, override val devicePublicKey: String) : GetRefreshTokenRequestDtoData()

    @Serializable
    @SerialName("passwordAuth")
    data class PasswordAuth(val email: String, val password: String, override val devicePublicKey: String) :
        GetRefreshTokenRequestDtoData()
}
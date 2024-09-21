package io.github.zidbrain.fchat.common.user.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeviceDto(val id: String, val userId: String, val publicKey: String)
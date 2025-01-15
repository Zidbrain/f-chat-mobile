package io.github.zidbrain.fchat.common.conversation.api.dto

import io.github.zidbrain.fchat.common.user.api.dto.DeviceDto
import kotlinx.serialization.Serializable

@Serializable
data class GetActiveDevicesRequest(val users: List<String>)

@Serializable
data class GetActiveDevicesResponse(val devices: List<DeviceDto>)
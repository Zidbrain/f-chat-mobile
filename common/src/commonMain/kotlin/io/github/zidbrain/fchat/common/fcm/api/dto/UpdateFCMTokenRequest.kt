package io.github.zidbrain.fchat.common.fcm.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateFCMTokenRequest(val token: String)
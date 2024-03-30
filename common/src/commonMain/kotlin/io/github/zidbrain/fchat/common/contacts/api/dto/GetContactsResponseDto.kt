package io.github.zidbrain.fchat.common.contacts.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class GetContactsResponseDto(val users: List<UserDto>)

@Serializable
data class UserDto(val id: String, val email: String, val displayName: String)
package io.github.zidbrain.fchat.common.contacts.model

import io.github.zidbrain.fchat.common.contacts.api.dto.UserDto

data class User(
    val email: String,
    val displayName: String
)

fun UserDto.toModel() = User(
    email = email,
    displayName = displayName
)
package io.github.zidbrain.fchat.common.contacts.model

import io.github.zidbrain.fchat.common.contacts.api.dto.UserDto

data class User(
    val id: String,
    val email: String,
    val displayName: String
)

fun UserDto.toModel() = User(
    id = id,
    email = email,
    displayName = displayName
)
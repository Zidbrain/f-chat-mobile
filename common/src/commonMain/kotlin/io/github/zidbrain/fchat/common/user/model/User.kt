package io.github.zidbrain.fchat.common.user.model

import io.github.zidbrain.fchat.common.contacts.api.dto.UserDto

data class User(val id: String, val name: String, val email: String)

fun UserDto.toModel() = User(id = id, name = displayName, email = email)
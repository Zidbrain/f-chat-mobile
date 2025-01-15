package io.github.zidbrain.fchat.common.contacts.model

import io.github.zidbrain.ContactEntity
import io.github.zidbrain.fchat.common.contacts.api.dto.UserDto

data class Contact(
    val id: String,
    val email: String,
    val displayName: String
)

fun UserDto.toModel() = Contact(
    id = id,
    email = email,
    displayName = displayName
)

fun ContactEntity.toModel() = Contact(
    id = id,
    email = email,
    displayName = displayName
)
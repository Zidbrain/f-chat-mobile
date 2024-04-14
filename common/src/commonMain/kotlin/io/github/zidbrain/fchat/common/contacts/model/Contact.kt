package io.github.zidbrain.fchat.common.contacts.model

import io.github.zidbrain.ContactEntity
import io.github.zidbrain.fchat.common.contacts.api.dto.ContactDto

data class Contact(
    val id: String,
    val email: String,
    val displayName: String
)

fun ContactDto.toModel() = Contact(
    id = id,
    email = email,
    displayName = displayName
)

fun ContactEntity.toModel() = Contact(
    id = id,
    email = email,
    displayName = displayName
)
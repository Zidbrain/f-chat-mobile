package io.github.zidbrain.fchat.common.contacts.api.dto

import io.github.zidbrain.ContactEntity
import kotlinx.serialization.Serializable

@Serializable
data class GetContactsResponseDto(val users: List<UserDto>)

@Serializable
data class UserDto(val id: String, val email: String, val displayName: String) {
    fun toEntity(publicKey: String) = ContactEntity(
        id = id,
        ownerId = publicKey,
        email = email,
        displayName = displayName
    )
}
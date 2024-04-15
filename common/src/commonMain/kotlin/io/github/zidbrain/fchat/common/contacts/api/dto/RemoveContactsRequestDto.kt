package io.github.zidbrain.fchat.common.contacts.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class RemoveContactsRequestDto(val contactsIds: List<String>)
package io.github.zidbrain.fchat.common.contacts.api

import io.github.zidbrain.fchat.common.contacts.api.dto.GetContactsResponseDto
import io.github.zidbrain.fchat.common.contacts.api.dto.RemoveContactsRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ContactsApi(private val client: HttpClient) {

    suspend fun getContacts(): GetContactsResponseDto = client.get("user/contacts") {
        contentType(ContentType.Application.Json)
    }.body()

    suspend fun searchUsers(searchString: String): GetContactsResponseDto = client.get("user/search") {
        parameter("searchString", searchString)
        contentType(ContentType.Application.Json)
    }.body()

    suspend fun addContact(id: String) {
        client.post("user/contacts/add") {
            parameter("contactId", id)
        }
    }

    suspend fun removeContacts(request: RemoveContactsRequestDto) {
        client.post("user/contacts/remove") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}
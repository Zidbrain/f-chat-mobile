package io.github.zidbrain.fchat.common.contacts.api

import io.github.zidbrain.fchat.common.contacts.api.dto.GetContactsResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
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
        client.post("user/addContact/$id")
    }
}
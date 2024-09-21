@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.zidbrain.fchat.common.contacts.repository

import io.github.zidbrain.fchat.common.contacts.api.ContactsApi
import io.github.zidbrain.fchat.common.contacts.api.dto.RemoveContactsRequestDto
import io.github.zidbrain.fchat.common.contacts.local.ContactsDao
import io.github.zidbrain.fchat.common.contacts.model.Contact
import io.github.zidbrain.fchat.common.contacts.model.toModel
import io.github.zidbrain.fchat.common.host.repository.SessionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest

class ContactsRepository(
    private val api: ContactsApi,
    private val dao: ContactsDao,
    private val sessionRepository: SessionRepository
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    val contacts = dao.getContactsForUserAsFlow(sessionRepository.session.userId)
        .distinctUntilChanged()
        .mapLatest {
            it.map { entity -> entity.toModel() }.ifEmpty {
                fetchContacts()
            }
        }

    fun getLocalContacts(): List<Contact> =
        dao.getContactsForUser(sessionRepository.session.userId)
            .map { it.toModel() }

    suspend fun fetchContacts(): List<Contact> {
        val response = api.getContacts()
        val userId = sessionRepository.session.userId
        dao.replaceContacts(userId, response.users.map { it.toEntity(userId) })
        return response.users.map { it.toModel() }
    }

    suspend fun addContact(contactId: String): Contact {
        api.addContact(contactId)
        fetchContacts()
        return contacts.first().find { it.id == contactId }!!
    }

    suspend fun searchContacts(local: Boolean, query: String): List<Contact> {
        val userId = sessionRepository.session.userId
        return if (local) dao.searchContacts(userId, query).map { it.toModel() }
        else {
            if (query.isBlank()) emptyList()
            else api.searchUsers(query).users.map { it.toModel() }
        }
    }

    suspend fun removeContacts(ids: List<String>) {
        val request = RemoveContactsRequestDto(ids)
        api.removeContacts(request)

        val userId = sessionRepository.session.userId
        dao.removeWithIds(userId, ids)
    }
}
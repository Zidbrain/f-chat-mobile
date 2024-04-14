@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.zidbrain.fchat.common.contacts.repository

import io.github.zidbrain.fchat.common.contacts.api.ContactsApi
import io.github.zidbrain.fchat.common.contacts.local.ContactsDao
import io.github.zidbrain.fchat.common.contacts.model.Contact
import io.github.zidbrain.fchat.common.contacts.model.toModel
import io.github.zidbrain.fchat.common.host.repository.SessionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest

class ContactsRepository(
    private val api: ContactsApi,
    private val dao: ContactsDao,
    private val sessionRepository: SessionRepository
) {

    val contacts by lazy {
        dao.getContactsForUserAsFlow(sessionRepository.session.devicePublicKey)
            .distinctUntilChanged()
            .mapLatest {
                it.map { entity -> entity.toModel() }.ifEmpty {
                    fetchContacts()
                }
            }
    }

    fun getLocalContacts(): List<Contact> =
        dao.getContactsForUser(sessionRepository.session.devicePublicKey)
            .map { it.toModel() }

    suspend fun fetchContacts(): List<Contact> {
        val response = api.getContacts()
        val publicKey = sessionRepository.session.devicePublicKey
        dao.replaceContacts(response.users.map { it.toEntity(publicKey) })
        return response.users.map { it.toModel() }
    }

    suspend fun addContact(contactId: String) {
        api.addContact(contactId)
        fetchContacts()
    }

    suspend fun searchContacts(local: Boolean, query: String): List<Contact> =
        if (local) dao.searchContacts(query).map { it.toModel() }
        else {
            if (query.isBlank()) emptyList()
            else api.searchUsers(query).users.map { it.toModel() }
        }
}
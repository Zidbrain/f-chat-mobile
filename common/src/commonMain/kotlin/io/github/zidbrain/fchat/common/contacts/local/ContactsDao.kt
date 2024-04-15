package io.github.zidbrain.fchat.common.contacts.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import io.github.zidbrain.ContactEntity
import io.github.zidbrain.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class ContactsDao(private val database: Database) {

    fun getContactsForUserAsFlow(publicKey: String): Flow<List<ContactEntity>> =
        database.contactEntityQueries.selectAll(publicKey).asFlow().mapToList(Dispatchers.IO)

    fun getContactsForUser(publicKey: String): List<ContactEntity> =
        database.contactEntityQueries.selectAll(publicKey).executeAsList()

    fun replaceContacts(contacts: List<ContactEntity>) = database.contactEntityQueries.transaction {
        database.contactEntityQueries.clear()
        contacts.forEach {
            database.contactEntityQueries.insert(it)
        }
    }

    fun searchContacts(name: String) = database.contactEntityQueries.searchContacts(name).executeAsList()

    fun removeWithIds(ids: List<String>) = database.contactEntityQueries.removeWithIds(ids)
}
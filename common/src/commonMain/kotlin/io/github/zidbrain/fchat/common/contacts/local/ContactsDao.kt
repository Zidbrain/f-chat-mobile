package io.github.zidbrain.fchat.common.contacts.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import io.github.zidbrain.ContactEntity
import io.github.zidbrain.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class ContactsDao(private val database: Database) {

    fun getContactsForUserAsFlow(ownerId: String): Flow<List<ContactEntity>> =
        database.contactEntityQueries.selectAll(ownerId).asFlow().mapToList(Dispatchers.IO)

    fun getContactsForUser(ownerId: String): List<ContactEntity> =
        database.contactEntityQueries.selectAll(ownerId).executeAsList()

    fun replaceContacts(ownerId: String, contacts: List<ContactEntity>) =
        database.contactEntityQueries.transaction {
            database.contactEntityQueries.clear(ownerId)
            contacts.forEach {
                database.contactEntityQueries.insert(it)
            }
        }

    fun searchContacts(ownerId: String, name: String) =
        database.contactEntityQueries.searchContacts(ownerId, name).executeAsList()

    fun removeWithIds(ownerId: String, ids: List<String>) =
        database.contactEntityQueries.removeWithIds(ownerId, ids)
}
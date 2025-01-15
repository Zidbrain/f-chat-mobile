package io.github.zidbrain.fchat.common.contacts.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import io.github.zidbrain.ContactEntity
import io.github.zidbrain.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ContactsDao(
     private val database: Database
) {

    fun getContactsForUserAsFlow(ownerId: String): Flow<List<ContactEntity>> =
        database.contactsQueries.selectAll(ownerId).asFlow().mapToList(Dispatchers.IO)

    fun getContactsForUser(ownerId: String): List<ContactEntity> =
        database.contactsQueries.selectAll(ownerId).executeAsList()

    fun replaceContacts(ownerId: String, contacts: List<ContactEntity>) =
        database.contactsQueries.transaction {
            database.contactsQueries.clear(ownerId)
            contacts.forEach {
                database.contactsQueries.insert(it)
            }
        }

    fun searchContacts(ownerId: String, name: String) =
        database.contactsQueries.searchContacts(ownerId, name).executeAsList()

    fun removeWithIds(ownerId: String, ids: List<String>) =
        database.contactsQueries.removeWithIds(ownerId, ids)
}
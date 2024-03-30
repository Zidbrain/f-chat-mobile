package io.github.zidbrain.fchat.common.contacts.viewmodel

import io.github.zidbrain.fchat.common.contacts.api.ContactsApi
import io.github.zidbrain.fchat.common.contacts.model.User
import io.github.zidbrain.fchat.mvi.MVIViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ContactsViewModel(private val contactsApi: ContactsApi) :
    MVIViewModel<ContactsAction, ContactsState, Nothing>(ContactsState.Loading) {
    private lateinit var allContacts: List<User>

    override fun onInit() = flow {
        val response = contactsApi.getContacts()
        emit(
            ContactsState.Content(
                contacts = response.users.map {
                    User(
                        email = it.email,
                        displayName = it.displayName
                    )
                },
                searchString = null
            )
        )
    }.errorState(ContactsState::Error)

    override fun handleAction(action: ContactsAction): Flow<ContactsState> = flow<ContactsState> {
        val searchString = when (action) {
            ContactsAction.InitSearch -> ""
            is ContactsAction.Search -> action.searchString
            ContactsAction.CancelSearch -> null
        }
        emit((state.value as ContactsState.Content).copy(searchString = searchString))

        val filtered = searchString?.let { string ->
            allContacts.filter {
                it.email.contains(string) || it.displayName.contains(string)
            }
        } ?: allContacts
        emit(
            ContactsState.Content(
                contacts = filtered,
                searchString = searchString
            )
        )
    }.errorState(ContactsState::Error)
}

sealed class ContactsState {
    data object Loading : ContactsState()
    data class Error(val error: Throwable) : ContactsState()
    data class Content(val contacts: List<User>, val searchString: String?) : ContactsState()
}

sealed class ContactsAction {
    data object InitSearch : ContactsAction()
    data class Search(val searchString: String) : ContactsAction()
    data object CancelSearch : ContactsAction()
}
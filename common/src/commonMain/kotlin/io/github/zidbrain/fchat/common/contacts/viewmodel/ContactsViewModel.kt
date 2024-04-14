package io.github.zidbrain.fchat.common.contacts.viewmodel

import io.github.zidbrain.fchat.common.contacts.model.Contact
import io.github.zidbrain.fchat.common.contacts.repository.ContactsRepository
import io.github.zidbrain.fchat.mvi.MVIViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ContactsViewModel(private val repository: ContactsRepository) :
    MVIViewModel<ContactsAction, ContactsState, ContactsEvent>(getInitState(repository)) {

    companion object {
        private fun getInitState(repository: ContactsRepository): ContactsState {
            val localContacts = repository.getLocalContacts()
            return if (localContacts.isEmpty()) ContactsState.Loading
            else localContacts.toState()
        }

        private fun List<Contact>.toState() =
            ContactsState.Content(
                contacts = this,
                topBarState = ContactsState.TopBarState.enabled(isNotEmpty()),
                isRefreshing = false
            )
    }


    override val initAction = buildAction {
        setStateBy(repository.contacts.map { it.toState() })
    }.onErrorSet(ContactsState::Error)

    override fun handleAction(action: ContactsAction) = buildAction {
        if (action !is ContactsAction.Refresh) cancelAction(ContactsAction.Refresh::class)

        when (action) {
            ContactsAction.Refresh -> requireState<ContactsState.Content> {
                setState { copy(isRefreshing = true) }
                repository.fetchContacts()
                setState { copy(isRefreshing = false) }
            }

            ContactsAction.DiscoverContacts -> {
                setState(
                    ContactsState.Content(
                        contacts = emptyList(), topBarState = ContactsState.TopBarState.search(
                            query = "", local = false, loading = false
                        ), isRefreshing = false
                    )
                )
            }

            ContactsAction.ClickCancelSearch -> {
                setState(repository.contacts.first().toState())
            }

            ContactsAction.ClickSearch -> {
                setState(
                    ContactsState.Content(
                        contacts = repository.contacts.first(),
                        topBarState = ContactsState.TopBarState.search(
                            query = "", local = true, loading = false
                        ),
                        isRefreshing = false
                    )
                )
            }

            is ContactsAction.SearchInput -> requireState<ContactsState.Content> {
                val searchState =
                    state.topBarState.searchState as ContactsState.SearchState.Searching
                setState {
                    copy(
                        topBarState = ContactsState.TopBarState.search(
                            query = action.query, local = searchState.local, loading = true
                        )
                    )
                }
                val filtered = repository.searchContacts(searchState.local, action.query)
                setState(
                    ContactsState.Content(
                        contacts = filtered, topBarState = ContactsState.TopBarState.search(
                            action.query, searchState.local, false
                        ), isRefreshing = false
                    )
                )
            }

            is ContactsAction.AddContact -> {
                setState(ContactsState.Loading)
                repository.addContact(action.id)
                raiseEvent(ContactsEvent.ContactAdded(action.displayName))
            }
        }
    }.onErrorSet(ContactsState::Error)
}

sealed class ContactsState(open val topBarState: TopBarState) {
    sealed class SearchState {
        data object Empty : SearchState()
        data class Searching(val query: String, val local: Boolean) : SearchState()
    }

    data class TopBarState(
        val searchState: SearchState,
        val searchButtonEnabled: Boolean,
        val searchIsCancelButton: Boolean,
        val showAddContactButton: Boolean,
        val loading: Boolean
    ) {
        companion object {
            val Disabled = TopBarState(
                searchState = SearchState.Empty,
                searchButtonEnabled = false,
                searchIsCancelButton = false,
                showAddContactButton = true,
                loading = false
            )

            fun enabled(enabled: Boolean = true) = Disabled.copy(searchButtonEnabled = enabled)

            fun search(query: String, local: Boolean, loading: Boolean) = TopBarState(
                searchState = SearchState.Searching(query, local),
                searchButtonEnabled = true,
                searchIsCancelButton = true,
                showAddContactButton = false,
                loading = loading
            )
        }
    }

    data object Loading : ContactsState(TopBarState.Disabled)
    data class Error(val error: Throwable) : ContactsState(TopBarState.Disabled)
    data class Content(
        val contacts: List<Contact>,
        override val topBarState: TopBarState,
        val isRefreshing: Boolean
    ) : ContactsState(topBarState)
}

sealed class ContactsAction {
    data object ClickSearch : ContactsAction()
    data object ClickCancelSearch : ContactsAction()
    data object DiscoverContacts : ContactsAction()
    data class SearchInput(val query: String) : ContactsAction()
    data class AddContact(val id: String, val displayName: String) : ContactsAction()
    data object Refresh : ContactsAction()
}

sealed class ContactsEvent {
    data class ContactAdded(val displayName: String) : ContactsEvent()
}
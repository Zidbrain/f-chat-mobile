package io.github.zidbrain.fchat.common.contacts.viewmodel

import io.github.zidbrain.fchat.common.contacts.model.Contact
import io.github.zidbrain.fchat.common.contacts.repository.ContactsRepository
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsState.TitleState
import io.github.zidbrain.fchat.common.nav.ConversationNavigationInfo
import io.github.zidbrain.fchat.common.user.repository.UserRepository
import io.github.zidbrain.fchat.common.util.replaceAt
import io.github.zidbrain.fchat.mvi.MVIViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ContactsViewModel(
    private val repository: ContactsRepository,
    private val userRepository: UserRepository
) :
    MVIViewModel<ContactsAction, ContactsState, ContactsEvent>(getInitState(repository)) {

    companion object {
        private fun getInitState(repository: ContactsRepository): ContactsState {
            val localContacts = repository.getLocalContacts()
            return if (localContacts.isEmpty()) ContactsState.Loading
            else localContacts.toState()
        }

        private fun List<Contact>.toState() =
            ContactsState.Content(
                contacts = toItemState(),
                topBarState = ContactsState.TopBarState.enabled(isNotEmpty()),
                removeDialog = null,
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
                        contacts = emptyList(),
                        topBarState = ContactsState.TopBarState.search(
                            query = "", local = false, loading = false
                        ),
                        removeDialog = null,
                        isRefreshing = false
                    )
                )
            }

            ContactsAction.ClickCancel -> {
                setState(repository.contacts.first().toState())
            }

            ContactsAction.ClickSearch -> {
                setState(
                    ContactsState.Content(
                        contacts = repository.contacts.first().toItemState(),
                        topBarState = ContactsState.TopBarState.search(
                            query = "", local = true, loading = false
                        ),
                        removeDialog = null,
                        isRefreshing = false
                    )
                )
            }

            is ContactsAction.SearchInput -> requireState<ContactsState.Content> {
                val titleState =
                    state.topBarState.title as TitleState.Searching
                setState {
                    copy(
                        topBarState = ContactsState.TopBarState.search(
                            query = action.query, local = titleState.local, loading = true
                        )
                    )
                }
                val filtered = repository.searchContacts(titleState.local, action.query)
                setState(
                    ContactsState.Content(
                        contacts = filtered.toItemState(),
                        topBarState = ContactsState.TopBarState.search(
                            action.query, titleState.local, false
                        ),
                        removeDialog = null,
                        isRefreshing = false
                    )
                )
            }

            is ContactsAction.ClickContact -> requireState<ContactsState.Content> {
                val prevState = state
                setState(ContactsState.Loading)
                if (prevState.topBarState.title is TitleState.Searching) {
                    val contact = repository.addContact(action.id)
                    raiseEvent(ContactsEvent.ContactAdded(contact.displayName))
                    return@requireState
                }

                val user = userRepository.getUser(id = action.id)
                setState(prevState)
                // TODO: Conversation with multiple users
                raiseEvent(
                    ContactsEvent.NavigateToConversation(
                        ConversationNavigationInfo.NewDirectMessageConversation(user.id)
                    )
                )
            }

            is ContactsAction.SelectContact -> requireState<ContactsState.Content> {
                val newContacts =
                    state.contacts.replaceAt(action.index) { it.copy(selected = true) }
                val newTopBarState = if (state.topBarState.title is TitleState.Searching)
                    state.topBarState.copy(removeContactAction = ContactsState.ActionButtonState())
                else {
                    val amountSelected = newContacts.count { it.selected }
                    ContactsState.TopBarState.selection(amountSelected)
                }
                setState {
                    copy(
                        contacts = newContacts,
                        topBarState = newTopBarState
                    )
                }
            }

            is ContactsAction.ClickRemoveContacts -> requireState<ContactsState.Content> {
                setState { copy(removeDialog = ContactsState.AlertDialogState.Content(state.contacts.count { it.selected })) }
            }

            is ContactsAction.RemoveContactsDialogResult -> requireState<ContactsState.Content> {
                if (!action.accept) {
                    setState { copy(removeDialog = null) }
                    return@requireState
                }

                setState { copy(removeDialog = ContactsState.AlertDialogState.Loading) }
                repository.removeContacts(state.contacts.filter { it.selected }
                    .map { it.contact.id })
                setState { copy(removeDialog = null) }
            }
        }
    }.onErrorSet(ContactsState::Error)
}

sealed class ContactsState(open val topBarState: TopBarState) {
    sealed class TitleState {
        data object Unspecified : TitleState()
        data class Searching(val query: String, val local: Boolean) : TitleState()
        data class Selecting(val amount: Int) : TitleState()
    }

    data class ActionButtonState(val enabled: Boolean = true, val visible: Boolean = true)

    sealed class NavButtonState {
        data object Back : NavButtonState()
        data object Cancel : NavButtonState()
    }

    data class TopBarState(
        val navButton: NavButtonState,
        val title: TitleState,
        val searchAction: ActionButtonState,
        val addContactAction: ActionButtonState,
        val removeContactAction: ActionButtonState,
        val loading: Boolean
    ) {
        companion object {
            val Disabled = enabled(false)

            fun enabled(enabled: Boolean = true) = TopBarState(
                navButton = NavButtonState.Back,
                title = TitleState.Unspecified,
                searchAction = ActionButtonState(enabled),
                addContactAction = ActionButtonState(),
                removeContactAction = ActionButtonState(visible = false),
                loading = false
            )

            fun search(query: String, local: Boolean, loading: Boolean) = TopBarState(
                navButton = NavButtonState.Cancel,
                title = TitleState.Searching(query, local),
                searchAction = ActionButtonState(visible = false),
                addContactAction = ActionButtonState(visible = false),
                removeContactAction = ActionButtonState(visible = false),
                loading = loading
            )

            fun selection(amount: Int) = TopBarState(
                navButton = NavButtonState.Cancel,
                title = TitleState.Selecting(amount),
                searchAction = ActionButtonState(visible = false),
                addContactAction = ActionButtonState(visible = false),
                removeContactAction = ActionButtonState(),
                loading = false
            )
        }
    }

    data class ContactItemState(val contact: Contact, val selected: Boolean)

    sealed class AlertDialogState {
        data object Loading : AlertDialogState()
        data class Content(val amount: Int) : AlertDialogState()
    }

    data object Loading : ContactsState(TopBarState.Disabled)
    data class Error(val error: Throwable) : ContactsState(TopBarState.Disabled)
    data class Content(
        val contacts: List<ContactItemState>,
        override val topBarState: TopBarState,
        val removeDialog: AlertDialogState?,
        val isRefreshing: Boolean
    ) : ContactsState(topBarState)
}

sealed class ContactsAction {
    data object ClickSearch : ContactsAction()
    data object ClickCancel : ContactsAction()
    data object DiscoverContacts : ContactsAction()
    data class SearchInput(val query: String) : ContactsAction()
    data class ClickContact(val id: String) : ContactsAction()
    data object Refresh : ContactsAction()

    data class SelectContact(val index: Int) : ContactsAction()
    data object ClickRemoveContacts : ContactsAction()
    data class RemoveContactsDialogResult(val accept: Boolean) : ContactsAction()
}

sealed class ContactsEvent {
    data class ContactAdded(val displayName: String) : ContactsEvent()
    data class NavigateToConversation(val info: ConversationNavigationInfo) : ContactsEvent()
}

fun List<Contact>.toItemState() = map {
    ContactsState.ContactItemState(
        contact = it,
        selected = false
    )
}
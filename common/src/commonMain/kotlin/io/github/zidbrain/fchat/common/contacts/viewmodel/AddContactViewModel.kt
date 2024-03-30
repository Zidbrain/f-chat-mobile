package io.github.zidbrain.fchat.common.contacts.viewmodel

import io.github.zidbrain.fchat.common.contacts.api.ContactsApi
import io.github.zidbrain.fchat.common.contacts.model.User
import io.github.zidbrain.fchat.common.contacts.model.toModel
import io.github.zidbrain.fchat.mvi.MVIViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AddContactViewModel(private val api: ContactsApi) :
    MVIViewModel<AddContactAction, AddContactState, Nothing>(
        AddContactState.Content("", false, emptyList())
    ) {
    private var users = emptyList<User>()

    override fun handleAction(action: AddContactAction): Flow<AddContactState> = flow {
        when (action) {
            is AddContactAction.Search -> {
                emit(
                    AddContactState.Content(
                        searchQuery = action.query,
                        loading = true,
                        users = users
                    )
                )

                val result = api.searchUsers(action.query)
                emit(
                    AddContactState.Content(
                        searchQuery = action.query,
                        loading = false,
                        users = result.users.map { it.toModel() }
                    )
                )
            }
        }
    }
}

sealed class AddContactAction {
    data class Search(val query: String) : AddContactAction()
}

sealed class AddContactState {
    data class Content(val searchQuery: String, val loading: Boolean, val users: List<User>) :
        AddContactState()
}
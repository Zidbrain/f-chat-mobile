package io.github.zidbrain.fchat.android.ui.contacts

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zidbrain.fchat.android.R
import io.github.zidbrain.fchat.android.ui.common.ErrorHandler
import io.github.zidbrain.fchat.android.ui.common.RoundedIcon
import io.github.zidbrain.fchat.android.ui.common.SelectableListItem
import io.github.zidbrain.fchat.android.ui.main.LocalSnackbarHostState
import io.github.zidbrain.fchat.common.contacts.model.Contact
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsAction
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsEvent
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsState
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsState.Content
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsState.Error
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsState.Loading
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsState.TitleState
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsState.TopBarState
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsViewModel
import io.github.zidbrain.fchat.common.contacts.viewmodel.toItemState
import io.github.zidbrain.fchat.common.nav.ConversationNavigationInfo
import io.github.zidbrain.fchat.util.CollectorEffect
import io.github.zidbrain.fchat.util.rememberCallbackState
import io.github.zidbrain.fchat.util.takeIfType
import org.koin.androidx.compose.koinViewModel

@Composable
fun ContactsPage(
    viewModel: ContactsViewModel = koinViewModel(),
    onBackPressed: () -> Unit,
    navigateToConversation: (ConversationNavigationInfo) -> Unit
) {
    BackHandler(onBack = onBackPressed)
    val state by viewModel.state.collectAsStateWithLifecycle()
    ContactsScreen(
        state = state,
        onBackPressed = onBackPressed,
        sendAction = { viewModel.sendAction(it) }
    )

    val snackbarHost = LocalSnackbarHostState.current
    CollectorEffect(flow = viewModel.events) {
        when (it) {
            is ContactsEvent.ContactAdded ->
                snackbarHost.showSnackbar(
                    message = "${it.displayName} has been added to your contact book"
                )

            is ContactsEvent.NavigateToConversation -> navigateToConversation(it.info)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactsScreen(
    state: ContactsState,
    onBackPressed: () -> Unit,
    sendAction: (ContactsAction) -> Unit
) = Surface(
    modifier = Modifier.fillMaxSize()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        ContactsTopAppBar(
            scrollBehavior = scrollBehavior,
            state = state,
            onBackPressed = onBackPressed,
            sendAction = sendAction
        )
        AnimatedContent(
            targetState = state,
            label = "Contacts Screen",
            contentKey = { it::class }
        ) {
            when (it) {
                is Content -> ContactsContent(
                    content = it,
                    sendAction = sendAction
                )

                is Error -> ErrorHandler(cause = it.error)
                Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ContactsTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    state: ContactsState,
    onBackPressed: () -> Unit,
    sendAction: (ContactsAction) -> Unit
) = Box {
    val requester = remember { FocusRequester() }
    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            AnimatedContent(
                targetState = state.topBarState.title,
                label = "",
                contentKey = { it::class }
            ) { titleState ->
                when (titleState) {
                    TitleState.Unspecified -> Text("Your contacts")
                    is TitleState.Searching -> {
                        BackHandler(onBack = { sendAction(ContactsAction.ClickCancel) })
                        var text by rememberCallbackState(titleState.query) {
                            sendAction(ContactsAction.SearchInput(it))
                        }
                        BasicTextField(
                            modifier = Modifier.focusRequester(requester),
                            value = text,
                            onValueChange = { text = it },
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                        LaunchedEffect(Unit) {
                            requester.requestFocus()
                        }
                    }

                    is TitleState.Selecting -> Text(titleState.amount.toString())
                }
            }
        },
        navigationIcon = {
            AnimatedContent(
                targetState = state.topBarState.navButton,
                label = "Nav Button",
                contentKey = { it::class }
            ) {
                when (it) {
                    ContactsState.NavButtonState.Back -> IconButton(onClick = onBackPressed) {
                        Icon(painterResource(R.drawable.outline_arrow_back_24), null)
                    }

                    ContactsState.NavButtonState.Cancel -> IconButton(
                        onClick = { sendAction(ContactsAction.ClickCancel) }
                    ) {
                        Icon(painterResource(R.drawable.outline_close_24), null)
                    }
                }
            }
        },
        actions = {
            AnimatedActionButton(state = state.topBarState.removeContactAction,
                onClick = { sendAction(ContactsAction.ClickRemoveContacts) }
            ) {
                Icon(painterResource(R.drawable.outline_delete_24), null)
            }
            AnimatedActionButton(
                state = state.topBarState.searchAction,
                onClick = { sendAction(ContactsAction.ClickSearch) }
            ) {
                Icon(painterResource(R.drawable.outline_search_24), null)
            }
            AnimatedActionButton(
                state = state.topBarState.addContactAction,
                onClick = { sendAction(ContactsAction.DiscoverContacts) }
            ) {
                Icon(painterResource(R.drawable.outline_person_add_alt_24), null)
            }
        }
    )
    if (state.topBarState.loading)
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        )
}

@Composable
private fun AnimatedActionButton(
    state: ContactsState.ActionButtonState,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(visible = state.visible) {
        IconButton(onClick = onClick, enabled = state.enabled) {
            content()
        }
    }
}

@Composable
private fun Contact(modifier: Modifier = Modifier, selected: Boolean, contact: Contact) =
    SelectableListItem(
        modifier = modifier,
        selected = selected
    ) {
        RoundedIcon(
            iconUrl = "icon",
            modifier = Modifier.padding(start = 10.dp)
        )
        Column {
            Text(
                modifier = Modifier.padding(start = 10.dp),
                text = contact.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                modifier = Modifier.padding(start = 10.dp, bottom = 4.dp),
                text = contact.email,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactsContent(
    content: Content,
    sendAction: (ContactsAction) -> Unit
) {
    content.removeDialog?.let { alertState ->
        fun result(accept: Boolean) {
            sendAction(ContactsAction.RemoveContactsDialogResult(accept))
        }

        val buttonsEnabled = alertState is ContactsState.AlertDialogState.Content
        AlertDialog(
            onDismissRequest = { result(false) },
            confirmButton = {
                TextButton(onClick = { result(true) }, enabled = buttonsEnabled) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { result(false) }, enabled = buttonsEnabled) {
                    Text("No")
                }
            },
            icon = { Icon(painterResource(R.drawable.outline_delete_24), null) },
            title = { Text("Removing contacts") },
            text = {
                AnimatedContent(
                    targetState = alertState,
                    label = "",
                    contentKey = { it::class },
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when (it) {
                        is ContactsState.AlertDialogState.Content -> Text(
                            pluralStringResource(
                                R.plurals.contacts_remove_alert_text,
                                it.amount,
                                it.amount
                            )
                        )

                        ContactsState.AlertDialogState.Loading -> CircularProgressIndicator()
                    }
                }
            }
        )
    }

    PullToRefreshBox(
        isRefreshing = content.isRefreshing,
        onRefresh = { sendAction(ContactsAction.Refresh) },
        modifier = Modifier.clipToBounds()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (content.contacts.isNotEmpty())
                contactList(content.contacts, content.topBarState, sendAction)
            else if (content.topBarState.title is TitleState.Unspecified) item {
                Text(
                    modifier = Modifier.padding(top = 40.dp),
                    text = "You do not have\nany contacts yet.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium,
                )
                TextButton(onClick = { sendAction(ContactsAction.DiscoverContacts) }) {
                    Text("Add some")
                }
            } else (content.topBarState.title as? TitleState.Searching)?.let {
                item {
                    if (it.query.isNotEmpty())
                        Text(
                            modifier = Modifier.padding(top = 40.dp),
                            text = "No users named ${it.query}.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    else  Text(
                        modifier = Modifier.padding(top = 40.dp),
                        text = "Enter the username",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.contactList(
    contacts: List<ContactsState.ContactItemState>,
    topBarState: TopBarState,
    sendAction: (ContactsAction) -> Unit
) {
    itemsIndexed(
        items = contacts,
        key = { _, it -> it.contact.id },
        contentType = { _, _ -> Contact::class }
    ) { i, (it, selected) ->
        Column(
            modifier = Modifier
                .animateItem()
                .fillMaxWidth()
        ) {
            Contact(
                modifier = Modifier
                    .combinedClickable(
                        onLongClick = {
                            if (!topBarState.title.takeIfType<TitleState.Searching> { !it.local })
                                sendAction(ContactsAction.SelectContact(i))
                        },
                        onClick = {
                            sendAction(ContactsAction.ClickContact(it.id))
                        }
                    ),
                selected = selected,
                contact = it
            )
            if (i != contacts.lastIndex)
                HorizontalDivider(
                    modifier = Modifier.padding(
                        start = 8.dp,
                        end = 8.dp
                    )
                )
        }
    }
}

@Composable
@Preview
private fun ContactsPreview() {
    ContactsScreen(
        state = Content(
            contacts = List(size = 10) {
                Contact("$it", "user$it@gmail.com", "User $it")
            }.toItemState(),
            topBarState = TopBarState.enabled(),
            removeDialog = null,
            isRefreshing = false
        ),
        onBackPressed = {},
        sendAction = {}
    )
}

@Composable
@Preview
private fun ContactsPreviewNoContacts() {
    ContactsScreen(
        state = Content(
            contacts = emptyList(),
            topBarState = TopBarState.Disabled,
            removeDialog = null,
            isRefreshing = false,
        ),
        onBackPressed = {},
        sendAction = {}
    )
}

@Composable
@Preview
private fun ContactsPreviewSearchContacts() {
    ContactsScreen(
        state = Content(
            contacts = emptyList(),
            topBarState = TopBarState.search(query = "something", local = false, loading = true),
            removeDialog = null,
            isRefreshing = false,
        ),
        onBackPressed = {},
        sendAction = {}
    )
}
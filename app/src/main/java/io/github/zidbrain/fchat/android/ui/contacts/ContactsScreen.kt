package io.github.zidbrain.fchat.android.ui.contacts

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zidbrain.fchat.android.R
import io.github.zidbrain.fchat.android.ui.common.ErrorHandler
import io.github.zidbrain.fchat.android.ui.main.LocalSnackbarHostState
import io.github.zidbrain.fchat.common.contacts.model.Contact
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsAction
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsEvent
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsState
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsState.Content
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsState.Error
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsState.Loading
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsState.SearchState
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsState.TopBarState
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsViewModel
import io.github.zidbrain.fchat.util.CollectorEffect
import io.github.zidbrain.fchat.util.rememberCallbackState
import org.koin.androidx.compose.koinViewModel

@Composable
fun ContactsPage(
    viewModel: ContactsViewModel = koinViewModel(),
    onBackPressed: () -> Unit
) {
    BackHandler(onBack = onBackPressed)
    val state by viewModel.state.collectAsStateWithLifecycle()
    ContactsContent(
        state = state,
        onBackPressed = onBackPressed,
        initSearch = { viewModel.sendAction(ContactsAction.ClickSearch) },
        search = { viewModel.sendAction(ContactsAction.SearchInput(it)) },
        cancelSearch = { viewModel.sendAction(ContactsAction.ClickCancelSearch) },
        discoverContacts = { viewModel.sendAction(ContactsAction.DiscoverContacts) },
        addContact = { id, displayName ->
            viewModel.sendAction(
                ContactsAction.AddContact(
                    id,
                    displayName
                )
            )
        },
        refresh = { viewModel.sendAction(ContactsAction.Refresh) }
    )

    val snackbarHost = LocalSnackbarHostState.current
    CollectorEffect(flow = viewModel.events) {
        when (it) {
            is ContactsEvent.ContactAdded ->
                snackbarHost.showSnackbar("${it.displayName} has been added to contacts")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactsContent(
    state: ContactsState,
    onBackPressed: () -> Unit,
    initSearch: () -> Unit,
    search: (String) -> Unit,
    cancelSearch: () -> Unit,
    discoverContacts: () -> Unit,
    addContact: (id: String, displayName: String) -> Unit,
    refresh: () -> Unit
) = Surface {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        ContactsTopAppBar(
            scrollBehavior = scrollBehavior,
            state = state,
            cancelSearch = cancelSearch,
            search = search,
            onBackPressed = onBackPressed,
            initSearch = initSearch,
            discoverContacts = discoverContacts
        )
        AnimatedContent(
            targetState = state,
            label = "Contacts Screen",
            contentKey = { it::class }
        ) {
            when (it) {
                is Content -> ContactsList(
                    content = it,
                    discoverContacts = discoverContacts,
                    addContact = addContact,
                    refresh = refresh
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
    cancelSearch: () -> Unit,
    search: (String) -> Unit,
    onBackPressed: () -> Unit,
    initSearch: () -> Unit,
    discoverContacts: () -> Unit
) = Box {
    val requester = remember { FocusRequester() }
    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            AnimatedContent(
                targetState = state.topBarState.searchState,
                label = "",
                contentKey = { it::class }
            ) { searchState ->
                when (searchState) {
                    SearchState.Empty -> Text("Your contacts")
                    is SearchState.Searching -> {
                        BackHandler(onBack = cancelSearch)
                        var text by rememberCallbackState(searchState.query, search)
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
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(painterResource(R.drawable.outline_arrow_back_24), null)
            }
        },
        actions = {
            AnimatedContent(
                targetState = state.topBarState.searchIsCancelButton,
                label = "Icon Change"
            ) {
                if (it)
                    IconButton(onClick = {
                        requester.freeFocus()
                        cancelSearch()
                    }) {
                        Icon(painterResource(R.drawable.outline_close_24), null)
                    }
                else
                    IconButton(
                        onClick = initSearch,
                        enabled = state.topBarState.searchButtonEnabled
                    ) {
                        Icon(painterResource(R.drawable.outline_search_24), null)
                    }
            }
            AnimatedVisibility(visible = state.topBarState.showAddContactButton) {
                IconButton(onClick = discoverContacts) {
                    Icon(painterResource(R.drawable.outline_person_add_alt_24), null)
                }
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
private fun Contact(modifier: Modifier = Modifier, contact: Contact) =
    Row(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .padding(start = 10.dp, top = 10.dp)
                .background(color = Color.LightGray, shape = CircleShape)
        )
        Column(
            modifier = Modifier.padding(top = 10.dp)
        ) {
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
private fun ContactsList(
    content: Content,
    discoverContacts: () -> Unit,
    addContact: (id: String, displayName: String) -> Unit,
    refresh: () -> Unit
) {
    val refreshState = rememberPullToRefreshState()
    if (refreshState.isRefreshing)
        LaunchedEffect(true) {
            refresh()
        }
    if (!content.isRefreshing)
        LaunchedEffect(false) {
            refreshState.endRefresh()
        }
    Box(
        modifier = Modifier
            .nestedScroll(refreshState.nestedScrollConnection)
            .clipToBounds()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (content.contacts.isNotEmpty())
                itemsIndexed(
                    items = content.contacts,
                    key = { _, it -> it.id },
                    contentType = { _, _ -> Contact::class }
                ) { i, it ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Contact(
                            modifier = Modifier
                                .clickable {
                                    if (content.topBarState.searchState !is SearchState.Empty)
                                        addContact(it.id, it.displayName)
                                }
                                .padding(bottom = 10.dp),
                            contact = it
                        )
                        if (i != content.contacts.lastIndex)
                            HorizontalDivider(
                                modifier = Modifier.padding(
                                    start = 8.dp,
                                    end = 8.dp
                                )
                            )
                    }
                }
            else if (content.topBarState.searchState is SearchState.Empty) item {
                Text(
                    modifier = Modifier.padding(top = 40.dp),
                    text = "You do not have\nany contacts yet.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium,
                )
                TextButton(onClick = discoverContacts) {
                    Text("Add some")
                }
            }
        }
        PullToRefreshContainer(state = refreshState, modifier = Modifier.align(Alignment.TopCenter))
    }
}

@Composable
@Preview
private fun ContactsPreview() {
    ContactsContent(
        state = Content(
            contacts = List(size = 10) {
                Contact("$it", "user$it@gmail.com", "User $it")
            },
            topBarState = TopBarState.enabled(),
            isRefreshing = false
        ),
        onBackPressed = {},
        initSearch = {},
        search = {},
        cancelSearch = {},
        discoverContacts = {},
        addContact = { _, _ -> },
        refresh = {}
    )
}

@Composable
@Preview
private fun ContactsPreviewNoContacts() {
    ContactsContent(
        state = Content(
            emptyList(), TopBarState.Disabled, false
        ),
        onBackPressed = {},
        initSearch = {},
        search = {},
        cancelSearch = {},
        discoverContacts = {},
        addContact = { _, _ -> },
        refresh = {}
    )
}
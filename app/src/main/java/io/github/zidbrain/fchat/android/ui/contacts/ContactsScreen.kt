package io.github.zidbrain.fchat.android.ui.contacts

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.zidbrain.fchat.android.R
import io.github.zidbrain.fchat.android.ui.common.ErrorHandler
import io.github.zidbrain.fchat.android.ui.theme.Style
import io.github.zidbrain.fchat.common.contacts.model.User
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsAction
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsState
import io.github.zidbrain.fchat.common.contacts.viewmodel.ContactsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ContactsPage(onBackPressed: () -> Unit) {
    val controller = rememberNavController()
    NavHost(navController = controller, startDestination = "contacts") {
        composable("contacts") {
            ContactsScreen(
                onBackPressed = onBackPressed,
                addContact = { controller.navigate("addContact") })
        }
        composable("addContact") {
            AddContactScreen()
        }
    }
}

@Composable
private fun ContactsScreen(
    viewModel: ContactsViewModel = koinViewModel(),
    onBackPressed: () -> Unit,
    addContact: () -> Unit
) {
    BackHandler(onBack = onBackPressed)
    val state by viewModel.state.collectAsStateWithLifecycle()
    ContactsContent(
        state = state,
        onBackPressed = onBackPressed,
        initSearch = { viewModel.sendAction(ContactsAction.InitSearch) },
        searchContacts = { viewModel.sendAction(ContactsAction.Search(it)) },
        cancelSearch = { viewModel.sendAction(ContactsAction.CancelSearch) },
        addContact = addContact
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactsContent(
    state: ContactsState,
    onBackPressed: () -> Unit,
    initSearch: () -> Unit,
    searchContacts: (String) -> Unit,
    cancelSearch: () -> Unit,
    addContact: () -> Unit
) = Surface {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        item {
            val content = remember(state) { state as? ContactsState.Content }
            TopAppBar(
                title = {
                    AnimatedContent(
                        targetState = content?.searchString,
                        label = "",
                        contentKey = { it != null }
                    ) {
                        if (it != null) OutlinedTextField(
                            value = it,
                            onValueChange = searchContacts
                        ) else Text("Your contacts")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(painterResource(R.drawable.outline_arrow_back_24), null)
                    }
                },
                actions = {
                    AnimatedContent(
                        targetState = content?.searchString,
                        label = "Icon Change",
                        contentKey = { it != null }
                    ) {
                        if (it == null) IconButton(
                            onClick = { initSearch() },
                            enabled = content != null && content.contacts.isNotEmpty()
                        ) {
                            Icon(painterResource(R.drawable.outline_search_24), null)
                        } else IconButton(onClick = { cancelSearch() }) {
                            Icon(painterResource(R.drawable.outline_close_24), null)
                        }
                    }
                    AnimatedVisibility(visible = content?.searchString == null) {
                        IconButton(onClick = addContact, enabled = content != null) {
                            Icon(painterResource(R.drawable.outline_person_add_alt_24), null)
                        }
                    }
                }
            )
        }
        item {
            AnimatedContent(
                modifier = Modifier.fillParentMaxSize(),
                targetState = state,
                label = "Contacts Screen"
            ) {
                when (it) {
                    is ContactsState.Content -> ContactsList(content = it, addContact = addContact)
                    is ContactsState.Error -> ErrorHandler(cause = it.error)
                    ContactsState.Loading -> Box(
                        modifier = Modifier.fillMaxWidth(),
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
}

@Composable
private fun ContactsList(content: ContactsState.Content, addContact: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (content.contacts.isNotEmpty())
            items(items = content.contacts, key = { it.email }, contentType = { User::class }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(start = 10.dp, top = 4.dp),
                        text = it.displayName,
                        style = Style.LargeBold
                    )
                    Text(
                        modifier = Modifier.padding(start = 10.dp, bottom = 4.dp),
                        text = it.email,
                        style = Style.Regular
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            top = 4.dp,
                            start = 8.dp,
                            end = 8.dp
                        )
                    )
                }
            }
        else item {
            Text(
                modifier = Modifier.padding(top = 40.dp),
                text = "You do not have\nany contacts yet.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
            )
            TextButton(onClick = addContact) {
                Text("Add some")
            }
        }
    }
}

@Composable
@Preview
private fun ContactsPreview() {
    ContactsContent(
        state = ContactsState.Content(
            List(10) {
                User("user$it@gmail.com", "User $it")
            }, null
        ),
        onBackPressed = {},
        initSearch = {},
        searchContacts = {},
        cancelSearch = {},
        addContact = {}
    )
}

@Composable
@Preview
private fun ContactsPreviewNoContacts() {
    ContactsContent(
        state = ContactsState.Content(
            emptyList(), null
        ),
        onBackPressed = {},
        initSearch = {},
        searchContacts = {},
        cancelSearch = {},
        addContact = {}
    )
}
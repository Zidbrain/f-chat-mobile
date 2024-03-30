package io.github.zidbrain.fchat.android.ui.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zidbrain.fchat.android.ui.common.SurfaceBox
import io.github.zidbrain.fchat.android.ui.theme.Style
import io.github.zidbrain.fchat.common.contacts.model.User
import io.github.zidbrain.fchat.common.contacts.viewmodel.AddContactAction
import io.github.zidbrain.fchat.common.contacts.viewmodel.AddContactState
import io.github.zidbrain.fchat.common.contacts.viewmodel.AddContactViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddContactScreen(viewModel: AddContactViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AddContactScreenContent(content = state as AddContactState.Content, search = {
        viewModel.sendAction(AddContactAction.Search(it))
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddContactScreenContent(content: AddContactState.Content, search: (String) -> Unit) =
    SurfaceBox(modifier = Modifier.fillMaxWidth()) {
        var active by remember { mutableStateOf(false) }
        SearchBar(
            modifier = Modifier.align(Alignment.Center),
            query = content.searchQuery,
            onQueryChange = search,
            onSearch = {},
            active = active,
            onActiveChange = { active = it }) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(items = content.users, key = { it.email }, contentType = { User::class }) {
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
            }
        }
    }

@Composable
@Preview
private fun AddContactScreenPreview() {
    AddContactScreenContent(content = AddContactState.Content("", false, List(10) {
        User("user$it@gmail.com", "User $it")
    }), search = {})
}
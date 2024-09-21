package io.github.zidbrain.fchat.android.ui.conversation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zidbrain.fchat.android.R
import io.github.zidbrain.fchat.android.ui.common.RoundedIcon
import io.github.zidbrain.fchat.common.chat.repository.Conversation
import io.github.zidbrain.fchat.common.conversation.viewmodel.ConversationListState
import io.github.zidbrain.fchat.common.conversation.viewmodel.ConversationListViewModel
import io.github.zidbrain.fchat.common.util.randomUUID
import io.github.zidbrain.fchat.util.formatForDisplay
import kotlinx.datetime.Clock
import org.koin.androidx.compose.koinViewModel
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun ConversationList(
    viewModel: ConversationListViewModel = koinViewModel(),
    onMenuClicked: () -> Unit,
    navigateToConversation: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ConversationList(
        state = state,
        onMenuClicked = onMenuClicked,
        navigateToConversation = navigateToConversation
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ConversationList(
    state: ConversationListState,
    onMenuClicked: () -> Unit,
    navigateToConversation: (String) -> Unit
) = Surface(
    modifier = Modifier
        .fillMaxSize()
        .safeContentPadding()
) {
    Column {
        CenterAlignedTopAppBar(
            navigationIcon = {
                IconButton(onClick = onMenuClicked) {
                    Icon(
                        painter = painterResource(R.drawable.outline_menu_24),
                        contentDescription = null
                    )
                }
            },
            title = {
                AnimatedContent(targetState = state.loading, label = "Loading anim") {
                    when (it) {
                        ConversationListState.LoadingState.Complete -> Text("F Chat")
                        ConversationListState.LoadingState.Loading -> Text("Connecting...")
                        ConversationListState.LoadingState.Error -> Text("An error occurred")
                    }
                }
            },
            actions = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        painter = painterResource(R.drawable.outline_search_24),
                        contentDescription = null
                    )
                }
            }
        )
        ConversationList(
            state = state,
            navigateToConversation = navigateToConversation
        )
    }
}

@Composable
private fun ConversationList(
    state: ConversationListState,
    navigateToConversation: (String) -> Unit
) = with(state) {
    LazyColumn {
        itemsIndexed(
            items = conversations,
            key = { _, it -> it.id },
            contentType = { _, _ -> Conversation::class }
        ) { i, it ->
            Row(modifier = Modifier
                .height(IntrinsicSize.Min)
                .clickable { navigateToConversation(it.id) }) {
                RoundedIcon(
                    modifier = Modifier.padding(start = 10.dp, top = 5.dp, bottom = 5.dp),
                    iconUrl = "icon"
                )
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            modifier = Modifier
                                .padding(start = 10.dp)
                                .weight(1f),
                            text = it.displayedName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                        it.lastMessage?.let { message ->
                            Text(
                                text = message.sentAt.formatForDisplay(),
                                modifier = Modifier.padding(end = 10.dp)
                            )
                        }
                    }
                    it.lastMessage?.let { message ->
                        Text(
                            modifier = Modifier.padding(start = 10.dp, bottom = 4.dp, end = 20.dp),
                            text = message.content,
                            style = MaterialTheme.typography.bodyLarge,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                }
            }

            if (i != conversations.lastIndex)
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
fun ChatPreview() {
    ConversationList(
        state = ConversationListState(
            conversations = List(5) {
                ConversationListState.ConversationOverviewModel(
                    id = randomUUID(),
                    displayedName = "Some guy $it",
                    lastMessage = if (it == 4) null
                    else ConversationListState.ChatMessageOverviewModel(
                        sentBySelf = it % 2 == 0,
                        content = "Message $it",
                        sentAt = Clock.System.now() - (it * 3).toDuration(DurationUnit.DAYS)
                    )
                )
            },
            loading = ConversationListState.LoadingState.Complete
        ),
        onMenuClicked = { },
        navigateToConversation = { }
    )
}
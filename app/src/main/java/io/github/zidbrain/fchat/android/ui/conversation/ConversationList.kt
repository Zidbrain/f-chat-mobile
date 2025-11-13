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
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zidbrain.fchat.android.R
import io.github.zidbrain.fchat.android.ui.common.RoundedIcon
import io.github.zidbrain.fchat.common.chat.model.Conversation
import io.github.zidbrain.fchat.common.chat.viewmodel.ChatState
import io.github.zidbrain.fchat.common.conversation.viewmodel.ConversationListState
import io.github.zidbrain.fchat.common.conversation.viewmodel.ConversationListViewModel
import io.github.zidbrain.fchat.common.user.model.User
import io.github.zidbrain.fchat.common.util.randomUUID
import io.github.zidbrain.fchat.util.formatForDisplay
import io.github.zidbrain.kotlin.inject.viewmodel.injectViewModel
import kotlin.time.Clock
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun ConversationList(
    viewModel: ConversationListViewModel = injectViewModel(),
    chatState: ChatState,
    onMenuClicked: () -> Unit,
    navigateToConversation: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ConversationList(
        chatState = chatState,
        state = state,
        onMenuClicked = onMenuClicked,
        navigateToConversation = navigateToConversation
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ConversationList(
    chatState: ChatState,
    state: ConversationListState,
    onMenuClicked: () -> Unit,
    navigateToConversation: (String) -> Unit
) = Surface(
    modifier = Modifier
        .fillMaxSize()
) {
    Column {
        val loadingState = remember(chatState, state) {
            when (chatState) {
                is ChatState.Connected -> state.loading
                is ChatState.Error -> ConversationListState.LoadingState.Error
                ChatState.Loading -> ConversationListState.LoadingState.Loading
            }
        }
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
                AnimatedContent(targetState = loadingState, label = "Loading anim") {
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
        if (state.conversations.isNotEmpty())
            ConversationList(
                state = state,
                navigateToConversation = navigateToConversation
            )
        else
            EmptyChatBanner()
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
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .clickable { navigateToConversation(it.id) }
            ) {
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
private fun EmptyChatBanner() = Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Icon(
        modifier = Modifier
            .padding(top = 140.dp)
            .size(144.dp),
        painter = painterResource(R.drawable.outline_chat_24),
        contentDescription = null
    )
    Text(
        modifier = Modifier.padding(top = 12.dp),
        text = stringResource(R.string.conversation_list_empty_banner_title),
        style = MaterialTheme.typography.headlineLarge
    )
    val linkColor = MaterialTheme.colorScheme.primary
    val subtitle = stringResource(R.string.conversation_list_empty_banner_subtitle)
    val link = remember {
        buildAnnotatedString {
            val linkStyle = SpanStyle(color = linkColor)
            withLink(
                LinkAnnotation.Clickable(
                    tag = "click",
                    styles = TextLinkStyles(linkStyle),
                    linkInteractionListener = {
                    }
                )
            ) {
                append(subtitle)
            }
        }
    }
    Text(
        modifier = Modifier.padding(top = 8.dp),
        text = link,
        style = MaterialTheme.typography.headlineSmall
    )
}

@Composable
@Preview
private fun ChatPreview() {
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
        chatState = ChatState.Connected(User("", "", "")),
        onMenuClicked = { },
        navigateToConversation = { }
    )
}

@Composable
@Preview
private fun EmptyChat() {
    ConversationList(
        state = ConversationListState(
            conversations = emptyList(),
            loading = ConversationListState.LoadingState.Complete
        ),
        chatState = ChatState.Connected(User("", "", "")),
        onMenuClicked = { },
        navigateToConversation = { }
    )
}
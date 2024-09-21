package io.github.zidbrain.fchat.android.ui.conversation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zidbrain.fchat.android.R
import io.github.zidbrain.fchat.android.ui.common.FullScreenLoadingIndicator
import io.github.zidbrain.fchat.android.ui.common.RoundedIcon
import io.github.zidbrain.fchat.android.ui.common.SystemBarsAppearance
import io.github.zidbrain.fchat.android.ui.theme.FChatTheme
import io.github.zidbrain.fchat.common.chat.repository.MessageStatus
import io.github.zidbrain.fchat.common.conversation.viewmodel.ConversationAction
import io.github.zidbrain.fchat.common.conversation.viewmodel.ConversationState
import io.github.zidbrain.fchat.common.conversation.viewmodel.ConversationState.Content
import io.github.zidbrain.fchat.common.conversation.viewmodel.ConversationState.ConversationMessage
import io.github.zidbrain.fchat.common.conversation.viewmodel.ConversationViewModel
import io.github.zidbrain.fchat.common.util.randomUUID
import io.github.zidbrain.fchat.util.formatForDisplay
import io.github.zidbrain.fchat.util.formatLocalTime
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

@Composable
fun ConversationScreen(
    viewModel: ConversationViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ConversationScreen(
        state = state,
        onAction = {
            viewModel.sendAction(it)
        },
        onBack = onBack
    )
}

@Composable
fun ConversationScreen(
    state: ConversationState,
    onAction: (ConversationAction) -> Unit,
    onBack: () -> Unit
) =
    AnimatedContent(
        state,
        label = "Conversation",
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
        contentKey = { it::class }
    ) {
        when (it) {
            is Content -> ConversationScreenContent(
                content = it,
                onAction = onAction,
                onBack = onBack
            )

            ConversationState.Loading -> FullScreenLoadingIndicator()
        }
    }

@Composable
private fun ConversationScreenContent(
    content: Content,
    onAction: (ConversationAction) -> Unit,
    onBack: () -> Unit
) {
    SystemBarsAppearance(
        isAppearanceLightNavigationBars = true
    )
    Scaffold(
        contentWindowInsets = WindowInsets.ime.add(WindowInsets.navigationBars),
        topBar = {
            ConversationTopAppBar(
                icon = content.iconUrl,
                name = content.name,
                onAction = onAction,
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.surfaceDim
                )
        ) {
            val lazyListState = rememberLazyListState()
            MessageColumn(
                lazyListState = lazyListState,
                messagesByDate = content.messagesByDate,
                modifier = Modifier.weight(1f)
            )
            MessageInputComponent(
                onSend = { message ->
                    onAction(ConversationAction.Send(message))
                    lazyListState.requestScrollToItem(0)
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageColumn(
    lazyListState: LazyListState,
    messagesByDate: List<ConversationState.MessagesByDate>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        verticalArrangement = Arrangement.Bottom,
        reverseLayout = true
    ) {
        messagesByDate.forEach { (date, messages) ->
            items(
                items = messages,
                key = { it.id },
                contentType = { ConversationMessage::class }
            ) {
                MessageChip(
                    message = it,
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(top = 6.dp, start = 10.dp, end = 10.dp)
                        .animateItem()
                )
            }
            stickyHeader(
                key = date.hashCode(),
                contentType = { LocalDate::class }
            ) {
                DateHeader(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(top = 10.dp),
                    date = date
                )
            }
        }
    }
}

@Composable
private fun DateHeader(modifier: Modifier = Modifier, date: LocalDate) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
            shape = MaterialTheme.shapes.large,
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 6.dp),
                text = date.formatForDisplay(),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MessageChip(message: ConversationMessage, modifier: Modifier = Modifier) = Box(
    modifier = modifier,
    contentAlignment = if (message.isSenderSelf) Alignment.CenterEnd else Alignment.CenterStart,
) {
    Surface(
        shape = ChatBlobShape(
            density = LocalDensity.current,
            cornerRadius = 10.dp,
            arrowSize = 10.dp,
            reverse = message.isSenderSelf
        ),
        color = if (message.isSenderSelf) MaterialTheme.colorScheme.surfaceTint
        else MaterialTheme.colorScheme.surface
    ) {
        FlowRow(
            modifier = Modifier.padding(all = 6.dp),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .padding(start = 6.dp),
                text = message.sentAt.formatLocalTime(),
                style = MaterialTheme.typography.labelSmall
            )
            AnimatedContent(
                modifier = Modifier.align(Alignment.Bottom),
                targetState = message.status,
                label = "status anim"
            ) {
                val resource = when (it) {
                    MessageStatus.Initial -> null
                    MessageStatus.Delivered -> R.drawable.outline_done_24
                    MessageStatus.Read -> R.drawable.outline_done_all_24
                }
                resource?.let {
                    Icon(
                        modifier = Modifier
                            .padding(start = 2.dp)
                            .size(14.dp),
                        painter = painterResource(resource),
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationTopAppBar(
    icon: String,
    name: String,
    onAction: (ConversationAction) -> Unit,
    onBack: () -> Unit
) {
    val containerColor = MaterialTheme.colorScheme.surfaceTint
    val contentColor = MaterialTheme.colorScheme.contentColorFor(containerColor)
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundedIcon(
                    iconUrl = icon,
                    modifier = Modifier.padding(start = 12.dp)
                )
                Text(
                    text = name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

            }
        },
        actions = {
            var expanded by remember { mutableStateOf(false) }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("Clear history") },
                    leadingIcon = {
                        Icon(
                            painterResource(id = R.drawable.outline_delete_sweep_24),
                            null
                        )
                    },
                    onClick = {
                        expanded = false
                        onAction(ConversationAction.ClearHistory)
                    }
                )
            }
            IconButton(onClick = { expanded = true }) {
                Icon(painterResource(R.drawable.outline_more_vert_24), null)
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(painterResource(R.drawable.outline_arrow_back_24), null)
            }
        }
    )
}

@Composable
private fun MessageInputComponent(
    modifier: Modifier = Modifier,
    onSend: (String) -> Unit
) {
    var value by remember { mutableStateOf("") }
    BasicTextField(
        modifier = modifier
            .wrapContentSize()
            .animateContentSize(alignment = Alignment.BottomCenter)
            .padding(all = 10.dp)
            .fillMaxWidth(),
        value = value,
        onValueChange = { value = it },
        textStyle = MaterialTheme.typography.bodyMedium,
        decorationBox = { textField ->
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.height(IntrinsicSize.Min)
                ) {
                    IconButton(onClick = {}) {
                        Icon(painterResource(R.drawable.outline_attach_file_24), null)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        textField()
                    }
                    IconButton(
                        enabled = value.isNotEmpty(),
                        onClick = {
                            onSend(value)
                            value = ""
                        }
                    ) {
                        Icon(painterResource(R.drawable.outline_send_24), null)
                    }
                }
            }
        }
    )
}

private fun message(content: String, isSenderSelf: Boolean, clock: Clock = Clock.System) =
    ConversationMessage(
        id = randomUUID(),
        status = MessageStatus.Delivered,
        senderName = if (isSenderSelf) "User" else "Jane Doe",
        senderIconUrl = "icon",
        content = content,
        isSenderSelf = isSenderSelf,
        sentAt = clock.now()
    )

@Preview
@Composable
private fun ConversationScreenPreview() = FChatTheme {
    val clock = Clock.System
    val state = Content(
        messagesByDate = listOf(
            ConversationState.MessagesByDate(
                clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date, listOf(
                    message(
                        isSenderSelf = false,
                        content = "Lorem Ipsum",
                    ),
                    message(
                        isSenderSelf = true,
                        content = "Lorem Ipsum response",
                    ),
                    message(
                        isSenderSelf = true,
                        content = "Lorem Ipsum response longdalskdowkdaowkdowakdpaokdpawkdpawkdp[aqkd",
                    )
                )
            ),
            ConversationState.MessagesByDate(
                clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.minus(
                    1,
                    DateTimeUnit.DAY
                ), listOf(
                    message(
                        isSenderSelf = false,
                        content = "Lorem Ipsum",
                    ),
                    message(
                        isSenderSelf = true,
                        content = "Lorem Ipsum response",
                    ),
                    message(
                        isSenderSelf = false,
                        content = "Lorem Ipsum response longdalskdowkdaowkdowakdpaokdpawkdpawkdp[aqkd",
                    )
                )
            )
        ),
        iconUrl = "icon",
        name = "Jane Doe"
    )
    ConversationScreen(
        state = state,
        onAction = { },
        onBack = { }
    )
}
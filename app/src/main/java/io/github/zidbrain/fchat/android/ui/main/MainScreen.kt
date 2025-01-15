package io.github.zidbrain.fchat.android.ui.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.zidbrain.fchat.android.R
import io.github.zidbrain.fchat.android.ui.common.RoundedIcon
import io.github.zidbrain.fchat.android.ui.contacts.ContactsPage
import io.github.zidbrain.fchat.android.ui.conversation.ConversationList
import io.github.zidbrain.fchat.common.chat.viewmodel.ChatState
import io.github.zidbrain.fchat.common.chat.viewmodel.ChatViewModel
import io.github.zidbrain.fchat.common.main.MainAction
import io.github.zidbrain.fchat.common.main.MainEvent
import io.github.zidbrain.fchat.common.main.MainViewModel
import io.github.zidbrain.fchat.common.nav.ConversationNavigationInfo
import io.github.zidbrain.fchat.common.user.model.User
import io.github.zidbrain.fchat.util.CollectorEffect
import io.github.zidbrain.fchat.util.showError
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }

@Composable
fun MainScreen(
    viewModel: MainViewModel = koinViewModel(),
    chatViewModel: ChatViewModel = koinViewModel(),
    startPage: MainScreenPage = MainScreenPage.Chat,
    onNavigateToConversation: (ConversationNavigationInfo) -> Unit
) {
    val snackbarHostState = LocalSnackbarHostState.current
    CollectorEffect(flow = viewModel.events) {
        when (it) {
            is MainEvent.Error -> snackbarHostState.showError()
        }
    }

    val chatState by chatViewModel.state.collectAsStateWithLifecycle()
    MainScreenMenu(
        chatState = chatState,
        startPage = startPage,
        onLogout = { viewModel.sendAction(MainAction.Logout) },
        onNavigateToConversation = onNavigateToConversation
    )
}

@Composable
private fun MainScreenMenu(
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    startPage: MainScreenPage = MainScreenPage.Chat,
    chatState: ChatState,
    onLogout: () -> Unit,
    onNavigateToConversation: (ConversationNavigationInfo) -> Unit,
    content: @Composable () -> Unit = {
        MainScreenContent(
            chatState = chatState,
            startPage = startPage,
            drawerState = drawerState,
            onNavigateToConversation = onNavigateToConversation
        )
    }
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                windowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Start)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                    ) {
                        RoundedIcon(
                            modifier = Modifier
                                .size(80.dp),
                            iconUrl = "icon",
                            imageSize = 64.dp
                        )

                        val text = (chatState as? ChatState.Connected)?.user?.name ?: "Loading..."
                        Text(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            text = text,
                            fontSize = 24.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text("Profile") },
                    icon = {
                        Icon(
                            painterResource(R.drawable.outline_account_circle_24),
                            null
                        )
                    },
                    selected = false,
                    onClick = { /*TODO*/ })
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    icon = { Icon(painterResource(R.drawable.outline_settings_24), null) },
                    selected = false,
                    onClick = { /*TODO*/ })
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Logout") },
                    icon = { Icon(painterResource(R.drawable.outline_logout_24), null) },
                    selected = false,
                    onClick = onLogout
                )
            }
        },
        gesturesEnabled = drawerState.isOpen,
        content = content
    )
}

@Composable
private fun MainScreenContent(
    startPage: MainScreenPage,
    drawerState: DrawerState,
    chatState: ChatState,
    navController: NavHostController = rememberNavController(),
    onNavigateToConversation: (ConversationNavigationInfo) -> Unit,
    pageContent: @Composable (MainScreenPage) -> Unit = {
        MainScreenPages(
            chatState = chatState,
            page = it,
            drawerState = drawerState,
            navController = navController,
            navigateToConversation = onNavigateToConversation
        )
    }
) {
    val pages = remember { MainScreenPage.entries }
    val currentBackStack by navController.currentBackStackEntryAsState()
    val selected =
        currentBackStack?.let { MainScreenPage.valueOf(it.destination.route!!) } ?: startPage
    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            MainScreenNavigationBar(
                pages = pages,
                selected = selected,
                onPageSelected = { navController.navigate(it.name) }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = LocalSnackbarHostState.current)
        },
    ) { paddingValues ->
        NavHost(
            modifier = Modifier.padding(paddingValues),
            navController = navController,
            startDestination = startPage.name,
            enterTransition = { enterTransitionForPage() },
            exitTransition = { exitTransitionForPage() },
            popEnterTransition = { enterTransitionForPage() },
            popExitTransition = { exitTransitionForPage() }
        ) {
            pages.forEach { page ->
                composable(page.name) {
                    pageContent(page)
                }
            }
        }
    }
}

@Composable
private fun MainScreenNavigationBar(
    pages: List<MainScreenPage>,
    selected: MainScreenPage,
    onPageSelected: (MainScreenPage) -> Unit
) {
    NavigationBar {
        pages.forEach {
            NavigationBarItem(
                selected = it == selected,
                onClick = { onPageSelected(it) },
                icon = {
                    Icon(
                        painter = painterResource(it.icon),
                        contentDescription = null
                    )
                },
                label = { Text(text = stringResource(it.label)) }
            )
        }
    }
}

@Composable
private fun MainScreenPages(
    navController: NavController,
    page: MainScreenPage,
    drawerState: DrawerState,
    chatState: ChatState,
    navigateToConversation: (ConversationNavigationInfo) -> Unit
) {
    val scope = rememberCoroutineScope()
    when (page) {
        MainScreenPage.Chat -> ConversationList(
            chatState = chatState,
            onMenuClicked = {
                scope.launch {
                    drawerState.open()
                }
            },
            navigateToConversation = {
                navigateToConversation(
                    ConversationNavigationInfo.ConversationId(it)
                )
            }
        )

        MainScreenPage.Contacts -> ContactsPage(
            onBackPressed = {
                navController.popBackStack()
            },
            navigateToConversation = {
                navController.navigate(MainScreenPage.Chat.name)
                navigateToConversation(it)
            }
        )
    }
}

enum class MainScreenPage(@DrawableRes val icon: Int, @StringRes val label: Int) {
    Chat(R.drawable.outline_chat_24, R.string.chat),
    Contacts(R.drawable.outline_person_24, R.string.contacts)
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransitionForPage(): EnterTransition {
    val initial = MainScreenPage.valueOf(initialState.destination.route!!).ordinal
    val target = MainScreenPage.valueOf(targetState.destination.route!!).ordinal
    if (target == initial) return EnterTransition.None

    return if (target > initial) slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left)
    else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right)
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransitionForPage(): ExitTransition {
    val initial = MainScreenPage.valueOf(initialState.destination.route!!).ordinal
    val target = MainScreenPage.valueOf(targetState.destination.route!!).ordinal
    if (target == initial) return ExitTransition.None

    return if (target > initial) slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
    else slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
}

@Composable
private fun MainScreenPreview(drawerValue: DrawerValue) {
    val drawerState = rememberDrawerState(initialValue = drawerValue)
    MainScreenMenu(
        drawerState = drawerState,
        chatState = ChatState.Connected(User("", "Username", "")),
        onLogout = {},
        onNavigateToConversation = {}
    ) {
        MainScreenContent(
            startPage = MainScreenPage.Chat,
            drawerState = drawerState,
            chatState = ChatState.Connected(User("", "Username", "")),
            onNavigateToConversation = {},
            pageContent = {}
        )
    }
}

@Composable
@Preview
private fun MainScreenDrawerClosedPreview() = MainScreenPreview(DrawerValue.Closed)

@Composable
@Preview
private fun MainScreenDrawerOpenedPreview() = MainScreenPreview(DrawerValue.Open)
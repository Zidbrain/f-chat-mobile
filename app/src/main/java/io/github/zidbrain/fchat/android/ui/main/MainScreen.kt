package io.github.zidbrain.fchat.android.ui.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.zidbrain.fchat.android.R
import io.github.zidbrain.fchat.android.ui.chat.ChatScreen
import io.github.zidbrain.fchat.android.ui.contacts.ContactsPage
import io.github.zidbrain.fchat.common.main.MainAction
import io.github.zidbrain.fchat.common.main.MainEvent
import io.github.zidbrain.fchat.common.main.MainViewModel
import io.github.zidbrain.fchat.util.CollectorEffect
import io.github.zidbrain.fchat.util.showError
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }

@Composable
fun MainScreen(
    viewModel: MainViewModel = koinViewModel(),
    startPage: MainScreenPage = MainScreenPage.Chat
) {
    val snackbarHostState = LocalSnackbarHostState.current
    CollectorEffect(flow = viewModel.events) {
        when (it) {
            is MainEvent.Error -> snackbarHostState.showError(it.cause)
        }
    }
    MainScreenMenu(
        startPage = startPage,
        onLogout = { viewModel.sendAction(MainAction.Logout) }
    )
}

@Composable
private fun MainScreenMenu(
    startPage: MainScreenPage = MainScreenPage.Chat,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerItem(
                    label = { Text("Profile") },
                    icon = { Icon(painterResource(R.drawable.outline_account_circle_24), null) },
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
        }
    ) {
        MainScreenContent(startPage = startPage, drawerState = drawerState)
    }
}

@Composable
private fun MainScreenContent(
    startPage: MainScreenPage,
    drawerState: DrawerState,
    navController: NavHostController = rememberNavController(),
    page: @Composable (MainScreenPage) -> Unit = {
        MainScreenPages(page = it, drawerState = drawerState, navController = navController)
    }
) {
    val pages = remember { MainScreenPage.entries }
    var selected by remember { mutableStateOf(startPage) }
    Scaffold(
        bottomBar = {
            MainScreenNavigationBar(
                pages = pages,
                selected = selected,
                onPageSelected = { selected = it }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = LocalSnackbarHostState.current)
        }
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
                    page(page)
                }
            }
        }

        LaunchedEffect(Unit) {
            navController.currentBackStackEntryFlow.collect {
                selected = MainScreenPage.valueOf(it.destination.route!!)
            }
        }
        LaunchedEffect(selected) {
            navController.navigate(selected.name)
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
    drawerState: DrawerState
) {
    val scope = rememberCoroutineScope()
    when (page) {
        MainScreenPage.Chat -> ChatScreen(onMenuClicked = {
            scope.launch {
                drawerState.open()
            }
        })

        MainScreenPage.Contacts -> ContactsPage(onBackPressed = {
            navController.popBackStack()
        })
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
    if (target == initial) ExitTransition.None

    return if (target > initial) slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
    else slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
}


@Composable
@Preview
private fun MainScreenPreview() {
    MainScreenMenu {

    }
}
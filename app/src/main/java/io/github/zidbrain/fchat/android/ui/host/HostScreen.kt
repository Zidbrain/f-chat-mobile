package io.github.zidbrain.fchat.android.ui.host

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zidbrain.fchat.android.ui.common.ErrorHandler
import io.github.zidbrain.fchat.android.ui.login.LoginScreen
import io.github.zidbrain.fchat.android.ui.main.MainScreen
import io.github.zidbrain.fchat.common.host.viewmodel.HostState
import io.github.zidbrain.fchat.common.host.viewmodel.HostViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HostScreen(viewModel: HostViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AnimatedContent(targetState = state, label = "Host Screen", contentKey = {
        when (it) {
            HostState.Loading, HostState.LogIn -> Unit
            else -> it::class
        }
    }) {
        when (it) {
            is HostState.Error -> ErrorHandler(cause = it.error)
            HostState.Loading, HostState.LogIn -> LoginScreen(sigInInButtonVisible = it is HostState.LogIn)
            HostState.Main -> MainScreen()
        }
    }
}
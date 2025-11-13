package io.github.zidbrain.fchat.android.ui.host

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import io.github.zidbrain.fchat.android.ui.common.ErrorHandler
import io.github.zidbrain.fchat.android.ui.login.LoginScreen
import io.github.zidbrain.fchat.android.ui.navigation.FChatNavHost
import io.github.zidbrain.fchat.common.host.viewmodel.HostState
import io.github.zidbrain.fchat.common.host.viewmodel.HostViewModel
import io.github.zidbrain.kotlin.inject.viewmodel.injectViewModel

@Composable
fun HostScreen(viewModel: HostViewModel = injectViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = state,
        label = "Host Screen",
        contentKey = { it::class }
    ) {
        PseudoNavEntry {
            when (it) {
                is HostState.Error -> ErrorHandler(cause = it.error)
                HostState.LogIn -> LoginScreen()
                HostState.Main -> FChatNavHost()
            }
        }
    }
}

private class SimpleViewModelStoreOwner() : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()

    fun clear() {
        viewModelStore.clear()
    }
}

@Composable
private fun PseudoNavEntry(content: @Composable () -> Unit) {
    val owner = remember { SimpleViewModelStoreOwner() }
    DisposableEffect(Unit) {
        onDispose { owner.clear() }
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides owner, content)
}
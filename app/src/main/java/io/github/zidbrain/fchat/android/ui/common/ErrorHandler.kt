package io.github.zidbrain.fchat.android.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import io.github.zidbrain.fchat.android.ui.main.LocalSnackbarHostState
import kotlinx.coroutines.launch

@Composable
fun ErrorHandler(cause: Throwable) {
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(cause) {
        snackbar.currentSnackbarData?.dismiss()
        scope.launch {
            snackbar.showSnackbar("Error occurred", withDismissAction = true)
        }
    }
}
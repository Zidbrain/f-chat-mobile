package io.github.zidbrain.fchat.util

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

@Composable
fun <T> CollectorEffect(flow: Flow<T>, collector: FlowCollector<T>) = LaunchedEffect(flow) {
    flow.collect(collector)
}

suspend fun SnackbarHostState.showError(): SnackbarResult =
    showSnackbar("Error occurred", withDismissAction = true)
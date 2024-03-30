package io.github.zidbrain.fchat.util

import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

@Composable
fun <T> CollectorEffect(flow: Flow<T>, collector: FlowCollector<T>) = LaunchedEffect(flow) {
    flow.collect(collector)
}

suspend fun SnackbarHostState.showError(cause: Throwable) {
    showSnackbar("Error occurred", withDismissAction = true)
    Log.e("Error", "Exception", cause)
}
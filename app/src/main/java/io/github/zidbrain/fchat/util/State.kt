package io.github.zidbrain.fchat.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun <T> rememberCallbackState(value: T, onUpdated: (T) -> Unit): MutableState<T> {
    val state = remember(value) { mutableStateOf(value) }
    LaunchedEffect(state.value) {
        if (state.value != value) onUpdated(state.value)
    }
    return state
}
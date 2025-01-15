package io.github.zidbrain.fchat.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope

actual abstract class ViewModel : ViewModel() {
    actual val viewModelScope: CoroutineScope
        get() = (this as ViewModel).viewModelScope
}
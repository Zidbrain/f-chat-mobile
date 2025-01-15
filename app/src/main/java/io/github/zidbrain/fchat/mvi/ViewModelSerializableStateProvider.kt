package io.github.zidbrain.fchat.mvi

import androidx.lifecycle.SavedStateHandle
import io.github.zidbrain.fchat.common.util.randomUUID

class ViewModelSerializableStateProvider<State>(
    private val savedStateHandle: SavedStateHandle,
    private val default: State,
) : ViewModelSavedStateProvider<State> {
    private val id = randomUUID()

    override fun getSavedStateOrDefault(): State = savedStateHandle.get<State>(id) ?: default

    override fun saveState(state: State) {
        savedStateHandle[id] = default
    }
}
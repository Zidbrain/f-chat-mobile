package io.github.zidbrain.fchat.mvi

interface ViewModelSavedStateProvider<State> {
    fun getSavedStateOrDefault(): State
    fun saveState(state: State)
}
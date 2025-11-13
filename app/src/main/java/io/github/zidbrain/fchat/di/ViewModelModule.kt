package io.github.zidbrain.fchat.di

import androidx.lifecycle.SavedStateHandle
import io.github.zidbrain.fchat.common.di.Named
import io.github.zidbrain.fchat.common.login.viewmodel.EmailAuthState
import io.github.zidbrain.fchat.mvi.ViewModelSavedStateProvider
import io.github.zidbrain.fchat.mvi.ViewModelSerializableStateProvider
import me.tatarka.inject.annotations.Inject

abstract class ViewModelModule {

    @Inject
    @Named(type = EmailAuthState::class)
    fun emailAuthStateProvider(savedStateHandle: SavedStateHandle): ViewModelSavedStateProvider<EmailAuthState> =
        ViewModelSerializableStateProvider(savedStateHandle, EmailAuthState.Empty)
}
package io.github.zidbrain.fchat.di

import androidx.lifecycle.SavedStateHandle
import io.github.zidbrain.fchat.common.login.viewmodel.EmailAuthState
import io.github.zidbrain.fchat.mvi.ViewModelSavedStateProvider
import io.github.zidbrain.fchat.mvi.ViewModelSerializableStateProvider
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module
class ViewModelModule {

    @Single
    @Named(type = EmailAuthState::class)
    fun emailAuthStateProvider(savedStateHandle: SavedStateHandle): ViewModelSavedStateProvider<EmailAuthState> =
        ViewModelSerializableStateProvider(savedStateHandle, EmailAuthState.Empty)
}
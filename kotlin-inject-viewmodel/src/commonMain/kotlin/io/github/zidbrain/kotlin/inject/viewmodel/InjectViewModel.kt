package io.github.zidbrain.kotlin.inject.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
inline fun <reified T : ViewModel> injectViewModel(
   // provider: ComponentViewModelProvider = checkNotNull(LocalComponentViewModelProvider.current) { "Can't find ComponentViewModelProvider" },
    vararg parameters: Any
): T {
    return viewModel<T> { TODO() }
}
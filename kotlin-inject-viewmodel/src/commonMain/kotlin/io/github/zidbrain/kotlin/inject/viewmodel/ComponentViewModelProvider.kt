package io.github.zidbrain.kotlin.inject.viewmodel

import androidx.lifecycle.ViewModel
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Scope
import kotlin.reflect.KClass

@Scope
annotation class ViewModelScope

@Component
@ViewModelScope
abstract class ViewModelComponent {
    abstract val viewModels: Map<KClass<out ViewModel>, Lazy<ViewModel>>
}

internal object ViewModelComponentHolder {

}
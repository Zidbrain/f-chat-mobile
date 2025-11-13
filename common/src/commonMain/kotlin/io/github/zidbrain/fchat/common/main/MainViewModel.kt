package io.github.zidbrain.fchat.common.main

import io.github.zidbrain.fchat.common.login.repository.LoginRepository
import io.github.zidbrain.fchat.mvi.MVIViewModel
import io.github.zidbrain.kotlin.inject.viewmodel.annotations.InjectViewModel

@InjectViewModel
class MainViewModel(private val loginRepository: LoginRepository) :
    MVIViewModel<MainAction, Unit, MainEvent>(Unit) {
    override fun handleAction(action: MainAction) = buildAction {
        when (action) {
            MainAction.Logout -> loginRepository.logout()
        }
    }.onErrorRaise(MainEvent::Error)

}

sealed class MainAction {
    data object Logout : MainAction()
}

sealed class MainEvent {
    data class Error(val cause: Throwable) : MainEvent()
}
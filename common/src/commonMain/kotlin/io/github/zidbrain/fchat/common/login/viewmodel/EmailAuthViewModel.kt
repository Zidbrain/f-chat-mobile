package io.github.zidbrain.fchat.common.login.viewmodel

import androidx.compose.runtime.Immutable
import io.github.zidbrain.fchat.common.login.repository.LoginRepository
import io.github.zidbrain.fchat.common.login.viewmodel.EmailAuthState.EmailValidation
import io.github.zidbrain.fchat.common.login.viewmodel.EmailAuthState.PasswordValidation
import io.github.zidbrain.fchat.mvi.MVIActionBuilder
import io.github.zidbrain.fchat.mvi.MVIViewModel
import io.github.zidbrain.fchat.mvi.ViewModelSavedStateProvider
import kotlinx.serialization.Serializable
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Named

@KoinViewModel
class EmailAuthViewModel(
    @Named(type = EmailAuthState::class)
    dataProvider: ViewModelSavedStateProvider<EmailAuthState>,
    private val loginRepository: LoginRepository
) :
    MVIViewModel<EmailAuthAction, EmailAuthState, Nothing>(dataProvider) {

    private fun validateEmail(email: String): EmailValidation {
        val split = email.split('@')
        if (split.size < 2)
            return EmailValidation.Incomplete
        if (split.size != 2 || !split[0].first().isLetterOrDigit()
            || !split[0].last().isLetterOrDigit()
        )
            return EmailValidation.InvalidFormat

        val domain = split[1].split(".")
        if (domain.size < 2)
            return EmailValidation.Incomplete
        if (domain.size != 2)
            return EmailValidation.InvalidFormat
        if (domain[1].length < 2 || domain[1].length > 3)
            return EmailValidation.InvalidFormat

        return EmailValidation.Valid
    }

    private fun validatePassword(password: String): PasswordValidation {
        if (password.length < 8)
            return PasswordValidation.InvalidLength
        if (!password.any { it.isLowerCase() } || !password.any { it.isUpperCase() } || !password.any { it.isDigit() })
            return PasswordValidation.InvalidSymbols

        return PasswordValidation.Valid
    }

    override fun handleAction(action: EmailAuthAction): MVIActionBuilder<EmailAuthState, Nothing> =
        buildAction {
            when (action) {
                is EmailAuthAction.EmailInput -> {
                    var emailValidation = validateEmail(action.email)
                    if (state.password.password.isNotEmpty() && emailValidation == EmailValidation.Incomplete)
                        emailValidation = EmailValidation.InvalidFormat
                    setState {
                        copy(
                            email = EmailAuthState.EmailState(
                                action.email,
                                emailValidation
                            )
                        )
                    }
                }

                EmailAuthAction.EmailLogIn -> {
                    try {
                        setState { copy(bottom = EmailAuthState.BottomState.Loading) }
                        loginRepository.loginWithEmail(state.email.email, state.password.password)
                    } catch (ex: Exception) {
                        setState { copy(bottom = EmailAuthState.BottomState.InvalidEmailPassword) }
                    }
                }

                EmailAuthAction.EmailSignIn -> {
                    try {
                        val validation = validatePassword(state.password.password)
                        if (validation != PasswordValidation.Valid)
                            setState { copy(password = state.password.copy(validation = validation)) }
                        else {
                            setState { copy(bottom = EmailAuthState.BottomState.Loading) }
                            loginRepository.signIn(state.email.email, state.password.password)
                        }
                    } catch (ex: Exception) {
                        setState { copy(bottom = EmailAuthState.BottomState.Error) }
                    }
                }

                is EmailAuthAction.PasswordInput -> {
                    if (action.password.isNotEmpty() && state.email.validation == EmailValidation.Incomplete)
                        setState {
                            copy(
                                email = EmailAuthState.EmailState(
                                    state.email.email,
                                    EmailValidation.InvalidFormat
                                )
                            )
                        }
                    setState {
                        copy(
                            password = EmailAuthState.PasswordState(
                                action.password,
                                PasswordValidation.Valid
                            )
                        )
                    }
                }
            }
        }
}

sealed class EmailAuthAction {
    data class EmailInput(val email: String) : EmailAuthAction()
    data class PasswordInput(val password: String) : EmailAuthAction()

    data object EmailLogIn : EmailAuthAction()
    data object EmailSignIn : EmailAuthAction()
}

@Immutable
@Serializable
data class EmailAuthState(
    val email: EmailState,
    val password: PasswordState,
    val bottom: BottomState
) {

    val loginEnabled = email.validation == EmailValidation.Valid
            && password.password.isNotEmpty()
            && bottom !is BottomState.Loading

    val signInEnabled = email.validation == EmailValidation.Valid
            && password.validation == PasswordValidation.Valid
            && bottom !is BottomState.Loading

    @Serializable
    data class EmailState(
        val email: String,
        val validation: EmailValidation
    )

    enum class EmailValidation {
        Valid, InvalidFormat, Incomplete
    }

    @Serializable
    data class PasswordState(
        val password: String,
        val validation: PasswordValidation
    )

    enum class PasswordValidation {
        Valid, InvalidSymbols, InvalidLength
    }

    @Serializable
    sealed class BottomState {
        data object Empty : BottomState()
        data object Error : BottomState()
        data object InvalidEmailPassword : BottomState()
        data object Loading : BottomState()
    }

    companion object {
        val Empty = EmailAuthState(
            email = EmailState("", EmailValidation.Incomplete),
            password = PasswordState("", PasswordValidation.Valid),
            bottom = BottomState.Empty
        )
    }
}
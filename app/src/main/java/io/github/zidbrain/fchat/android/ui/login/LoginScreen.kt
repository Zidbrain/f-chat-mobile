package io.github.zidbrain.fchat.android.ui.login

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zidbrain.fchat.android.R
import io.github.zidbrain.fchat.android.ui.theme.Style
import io.github.zidbrain.fchat.common.login.viewmodel.EmailAuthAction
import io.github.zidbrain.fchat.common.login.viewmodel.EmailAuthState
import io.github.zidbrain.fchat.common.login.viewmodel.EmailAuthState.BottomState
import io.github.zidbrain.fchat.common.login.viewmodel.EmailAuthState.EmailValidation
import io.github.zidbrain.fchat.common.login.viewmodel.EmailAuthState.PasswordValidation
import io.github.zidbrain.fchat.common.login.viewmodel.EmailAuthViewModel
import io.github.zidbrain.fchat.common.login.viewmodel.LoginAction
import io.github.zidbrain.fchat.common.login.viewmodel.LoginState
import io.github.zidbrain.fchat.common.login.viewmodel.LoginViewModel
import io.github.zidbrain.fchat.util.rememberCallbackState
import io.github.zidbrain.kotlin.inject.viewmodel.injectViewModel

@Composable
fun LoginScreen(viewModel: LoginViewModel = injectViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LoginScreenContent(
        state = state,
        onAction = { viewModel.sendAction(it) }
    )
}

@Composable
private fun LoginScreenContent(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
    emailInput: @Composable () -> Unit = {
        LoginScreenEmailInput(onBack = { onAction(LoginAction.ToButtons) })
    }
) = Surface(
    modifier = Modifier
        .safeDrawingPadding()
        .fillMaxSize()
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Logo(modifier = Modifier.padding(bottom = 20.dp))
        AnimatedContent(
            targetState = state,
            contentAlignment = Alignment.Center,
            label = "expand animation",
            contentKey = { it::class }
        ) {
            when (it) {
                LoginState.Empty -> {}
                LoginState.Loading -> CircularProgressIndicator()
                is LoginState.Buttons -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                    ) {
                        LoginScreenButtons(onAction)
                        AnimatedVisibility(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            visible = it.error != null
                        ) {
                            val linkColor = MaterialTheme.colorScheme.primary
                            val linkStyle =
                                SpanStyle(color = linkColor, fontWeight = FontWeight.Bold)
                            val tryAgain = remember {
                                buildAnnotatedString {
                                    append("Unfortunately an error occurred while trying to login.\n")
                                    withLink(
                                        LinkAnnotation.Clickable(
                                            tag = "tryAgain",
                                            styles = TextLinkStyles(linkStyle),
                                            linkInteractionListener = {
                                                onAction(LoginAction.GoogleLogin)
                                            }
                                        )
                                    ) {
                                        append("Please try again")
                                    }
                                }
                            }
                            Text(
                                text = tryAgain,
                                textAlign = TextAlign.Center
                            )
                        }

                    }
                }

                is LoginState.EmailInput -> emailInput()
            }
        }
    }
}

@Composable
private fun LoginScreenButtons(onAction: (LoginAction) -> Unit) =
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        GoogleLoginButton(
            onLogin = {
                onAction(LoginAction.GoogleLogin)
            }
        )
        EmailLoginButton(
            onToEmail = {
                onAction(LoginAction.ToEmailInput)
            }
        )
    }

@Composable
private fun GoogleLoginButton(onLogin: () -> Unit) {
    TextButton(
        border = BorderStroke(1.dp, Color.Black),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
        onClick = onLogin
    ) {
        Image(painterResource(R.drawable.ic_google), null)
        Text(
            text = "Sign in with Google",
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

@Composable
private fun EmailLoginButton(onToEmail: () -> Unit) {
    TextButton(
        border = BorderStroke(1.dp, Color.Black),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
        onClick = onToEmail
    ) {
        Image(painterResource(R.drawable.outline_email_24), null)
        Text(
            text = "Sign in with Email/Password",
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

@Composable
private fun LoginScreenEmailInput(
    viewModel: EmailAuthViewModel = injectViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LoginScreenEmailInput(
        state = state,
        onAction = { viewModel.sendAction(it) },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginScreenEmailInput(
    state: EmailAuthState,
    onAction: (EmailAuthAction) -> Unit,
    onBack: () -> Unit
) =
    Column(
        modifier = Modifier.width(IntrinsicSize.Min),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BackHandler(onBack = onBack)

        IconButton(
            modifier = Modifier
                .align(Alignment.Start)
                .offset(x = (-10).dp),
            onClick = onBack,
            enabled = state.bottom !is BottomState.Loading
        ) {
            Icon(painterResource(R.drawable.outline_arrow_back_24), null)
        }

        var email by rememberCallbackState(state.email.email) {
            onAction(EmailAuthAction.EmailInput(it))
        }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            isError = state.email.validation == EmailValidation.InvalidFormat,
            supportingText = {
                if (state.email.validation == EmailValidation.InvalidFormat)
                    Text("Invalid email format")
            },
            enabled = state.bottom !is BottomState.Loading,
        )

        val password = remember { TextFieldState(state.password.password) }
        LaunchedEffect(password.text) {
            onAction(EmailAuthAction.PasswordInput(password.text.toString()))
        }
        val interactionSource = remember { MutableInteractionSource() }
        var showPassword by remember { mutableStateOf(false) }
        BasicSecureTextField(
            modifier = Modifier
                .padding(top = 4.dp)
                .defaultMinSize(
                    minWidth = OutlinedTextFieldDefaults.MinWidth,
                    minHeight = OutlinedTextFieldDefaults.MinHeight
                ),
            state = password,
            interactionSource = interactionSource,
            textObfuscationMode = if (showPassword) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped,
            inputTransformation = InputTransformation.maxLength(64),
            enabled = state.bottom !is BottomState.Loading,
            decorator = {
                OutlinedTextFieldDefaults.DecorationBox(
                    value = password.text.toString(),
                    innerTextField = it,
                    enabled = state.bottom !is BottomState.Loading,
                    singleLine = true,
                    label = { Text("Password") },
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    trailingIcon = {
                        if (password.text.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    showPassword = !showPassword
                                }
                            ) {
                                if (showPassword)
                                    Icon(painterResource(R.drawable.outline_visibility_24), null)
                                else
                                    Icon(
                                        painterResource(R.drawable.outline_visibility_off_24),
                                        null
                                    )
                            }
                        }
                    },
                    isError = state.password.validation != PasswordValidation.Valid,
                    supportingText = {
                        when (state.password.validation) {
                            PasswordValidation.Valid -> {}
                            PasswordValidation.InvalidSymbols -> Text("Password must contain at least one digit, lowercase and uppercase letter")
                            PasswordValidation.InvalidLength -> Text("Password must be at least 8 characters long")
                        }
                    }
                )
            }
        )

        Row(
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FilledTonalButton(
                enabled = state.loginEnabled,
                onClick = {
                    onAction(EmailAuthAction.EmailLogIn)
                }
            ) {
                Text("Log In")
            }
            FilledTonalButton(
                enabled = state.signInEnabled,
                onClick = {
                    onAction(EmailAuthAction.EmailSignIn)
                }
            ) {
                Text("Sign In")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        when (state.bottom) {
            BottomState.Empty -> {}
            BottomState.Error -> Text("An error occurred while attempting to log in")
            BottomState.InvalidEmailPassword -> Text("Invalid email or password")
            BottomState.Loading -> CircularProgressIndicator()
        }
    }

@Composable
private fun Logo(modifier: Modifier = Modifier) {
    Text(modifier = modifier, text = "F Chat", style = Style.Logo)
}

@Preview
@Composable
private fun LoginScreenLoadingPreview() {
    LoginScreenContent(state = LoginState.Loading, onAction = {})
}

@Preview
@Composable
private fun LoginScreenPreview() {
    LoginScreenContent(
        state = LoginState.Buttons(),
        onAction = {}
    )
}

@Preview
@Composable
private fun LoginScreenErrorPreview() {
    LoginScreenContent(
        state = LoginState.Buttons(Throwable()),
        onAction = {}
    )
}

@Preview
@Composable
private fun LoginScreenEmailInputPreview() {
    LoginScreenContent(
        state = LoginState.EmailInput,
        onAction = {},
        emailInput = {
            LoginScreenEmailInput(
                state = EmailAuthState.Empty.copy(bottom = BottomState.Loading),
                onAction = {},
                onBack = {}
            )
        }
    )
}
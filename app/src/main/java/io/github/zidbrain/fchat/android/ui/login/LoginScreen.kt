package io.github.zidbrain.fchat.android.ui.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zidbrain.fchat.android.ui.theme.Style
import io.github.zidbrain.fchat.common.login.viewmodel.LoginAction
import io.github.zidbrain.fchat.common.login.viewmodel.LoginState
import io.github.zidbrain.fchat.common.login.viewmodel.LoginViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun LoginScreen(viewModel: LoginViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LoginScreenContent(
        state = state,
        onTokenAcquired = { token, email -> viewModel.sendAction(LoginAction.Login(token, email)) }
    )
}

@Composable
private fun LoginScreenContent(
    state: LoginState,
    onTokenAcquired: (String, String) -> Unit
) = Surface(
    modifier = Modifier
        .fillMaxSize()
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Logo()
        AnimatedVisibility(visible = state is LoginState.Content || state is LoginState.Error) {
            AnimatedContent(
                modifier = Modifier.padding(top = 20.dp),
                targetState = state,
                contentAlignment = Alignment.Center,
                label = "expand animation"
            ) {
                when (it) {
                    LoginState.Empty -> {}
                    is LoginState.Content -> {
                        if (it.loading) CircularProgressIndicator()
                        else LoginButton(onTokenAcquired)
                    }

                    is LoginState.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LoginButton(onTokenAcquired)
                        Text(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            text = "Unfortunately an error occurred while trying to login.\nPlease try again.",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginButton(onTokenAcquired: (String, String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val manager = koinInject<CredentialManager>()
    TextButton(onClick = {
        scope.launch {
            val token =
                launchAuthForm(manager, context) ?: return@launch
            onTokenAcquired(token.idToken, token.id)
        }
    }) {
        Text("Sign in")
    }
}

@Composable
private fun Logo() {
    Text(text = "F Chat", style = Style.Logo)
}

@Preview
@Composable
private fun LoginScreenPreview() {
    LoginScreenContent(state = LoginState.Content(true), onTokenAcquired = { _, _ -> })
}
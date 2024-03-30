package io.github.zidbrain.fchat.android.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import io.github.zidbrain.fchat.android.ui.theme.Style
import io.github.zidbrain.fchat.common.login.viewmodel.LoginAction
import io.github.zidbrain.fchat.common.login.viewmodel.LoginViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun LoginScreen(viewModel: LoginViewModel = koinViewModel(), sigInInButtonVisible: Boolean) {
    LoginScreenContent(
        sigInInButtonVisible = sigInInButtonVisible,
        onTokenAcquired = { token, email -> viewModel.sendAction(LoginAction.Login(token, email)) }
    )
}

@Composable
private fun LoginScreenContent(
    sigInInButtonVisible: Boolean,
    onTokenAcquired: (String, String) -> Unit
) = Surface {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Logo()
            AnimatedVisibility(visible = sigInInButtonVisible) {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val manager = koinInject<CredentialManager>()
                TextButton(onClick = {
                    scope.launch {
                        val token = launchAuthForm(manager, context) ?: return@launch
                        onTokenAcquired(token.idToken, token.id)
                    }
                }, modifier = Modifier.padding(top = 20.dp)) {
                    Text("Sign in")
                }
            }
        }
    }
}

@Composable
private fun Logo() {
    Text(text = "F Chat", style = Style.Logo)
}

@Preview
@Composable
private fun LoginScreenPreview() {
    LoginScreenContent(sigInInButtonVisible = false, onTokenAcquired = { _, _ -> })
}
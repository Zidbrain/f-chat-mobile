package io.github.zidbrain.fchat.android.ui.login

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.zidbrain.fchat.android.BuildConfig

private const val SERVER_CLIENT_ID = BuildConfig.SERVER_CLIENT_ID
private val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
    .setFilterByAuthorizedAccounts(false)
    .setServerClientId(SERVER_CLIENT_ID)
    .build()
private val request: GetCredentialRequest = GetCredentialRequest.Builder()
    .addCredentialOption(googleIdOption)
    .build()

suspend fun launchAuthForm(
    credentialManager: CredentialManager,
    context: Context
): GoogleIdTokenCredential? {
    val result = try {
        credentialManager.getCredential(
            request = request,
            context = context,
        )
    } catch (ex: GetCredentialCancellationException) {
        return null
    }

    val credential = result.credential
    if (credential is CustomCredential) {
        return GoogleIdTokenCredential
            .createFrom(credential.data)
    } else throw Exception()
}
package io.github.zidbrain.fchat.common.login

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

actual class GoogleAuthForm(
    private val context: Context,
    private val credentialManager: CredentialManager,
    serverClientId: String
) {
    actual suspend fun launch(): GoogleIdTokenCredential? {
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

    private val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(serverClientId)
        .build()
    private val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
}
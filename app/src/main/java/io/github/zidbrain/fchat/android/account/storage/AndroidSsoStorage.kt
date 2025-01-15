package io.github.zidbrain.fchat.android.account.storage

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import io.github.zidbrain.fchat.common.account.storage.SsoStorage
import org.koin.core.annotation.Single

@Single
class AndroidSsoStorage(private val credentialManager: CredentialManager) : SsoStorage {
    override suspend fun clear() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}
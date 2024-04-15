package io.github.zidbrain.fchat.common.account.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import io.github.zidbrain.fchat.common.host.repository.UserSessionInfo

class AndroidEncryptedStorage(context: Context) : EncryptedStorage {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val prefs = EncryptedSharedPreferences.create(
        FILE_NAME,
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    private val editor = prefs.edit()

    override var userSession: UserSessionInfo?
        get() {
            val token = prefs.getString(REFRESH_TOKEN, null) ?: return null
            val id = prefs.getString(USER_ID, null) ?: return null
            return UserSessionInfo(token, id)
        }
        set(value) {
            editor
                .putString(REFRESH_TOKEN, value?.refreshToken)
                .putString(USER_ID, value?.userId)
                .commit()
        }

    private companion object {
        const val FILE_NAME = "prefs_encrypted"
        const val REFRESH_TOKEN = "refreshToken"
        const val USER_ID = "userId"
    }
}
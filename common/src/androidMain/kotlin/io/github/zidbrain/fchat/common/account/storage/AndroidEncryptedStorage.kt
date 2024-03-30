package io.github.zidbrain.fchat.common.account.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

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

    override var refreshToken: String?
        get() = prefs.getString(REFRESH_TOKEN, null)
        set(value) {
            editor
                .putString(REFRESH_TOKEN, value)
                .commit()
        }

    private companion object {
        const val FILE_NAME = "prefs_encrypted"
        const val REFRESH_TOKEN = "refreshToken"
    }
}
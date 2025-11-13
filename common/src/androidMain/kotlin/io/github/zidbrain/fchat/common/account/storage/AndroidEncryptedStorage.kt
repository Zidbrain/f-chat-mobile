package io.github.zidbrain.fchat.common.account.storage

import android.content.Context
import io.github.zidbrain.fchat.common.di.AppScope
import io.github.zidbrain.fchat.common.host.repository.UserSessionInfo
import me.tatarka.inject.annotations.Inject

class AndroidEncryptedStorage(context: Context) : EncryptedStorage {

    private val prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    private val editor = prefs.edit()

    override var userSession: UserSessionInfo?
        get() {
            val token = prefs.getString(REFRESH_TOKEN, null) ?: return null
            val id = prefs.getString(USER_ID, null) ?: return null
            val email = prefs.getString(EMAIL, null) ?: return null
            return UserSessionInfo(token, id, email)
        }
        set(value) {
            editor
                .putString(REFRESH_TOKEN, value?.refreshToken)
                .putString(USER_ID, value?.userId)
                .putString(EMAIL, value?.email)
                .commit()
        }

    override var fcmToken: String?
        get() = prefs.getString(FCM_TOKEN, null)
        set(value) {
            editor
                .putString(FCM_TOKEN, value)
                .commit()
        }

    private companion object {
        const val FILE_NAME = "prefs_encrypted"
        const val REFRESH_TOKEN = "refreshToken"
        const val USER_ID = "userId"
        const val EMAIL = "email"
        const val FCM_TOKEN = "fcm_token"
    }
}
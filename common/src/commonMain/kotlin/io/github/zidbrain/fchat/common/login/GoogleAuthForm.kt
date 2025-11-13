package io.github.zidbrain.fchat.common.login

import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

expect class GoogleAuthForm {
    suspend fun launch(): GoogleIdTokenCredential?
}
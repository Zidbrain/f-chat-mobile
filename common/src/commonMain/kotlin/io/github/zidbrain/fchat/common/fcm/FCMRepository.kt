package io.github.zidbrain.fchat.common.fcm

import android.util.Log
import io.github.zidbrain.fchat.common.di.AppScope
import io.github.zidbrain.fchat.common.fcm.api.FCMApi
import io.github.zidbrain.fchat.common.fcm.api.dto.UpdateFCMTokenRequest
import io.github.zidbrain.fchat.common.host.repository.SessionRepository
import io.github.zidbrain.fchat.common.host.repository.UserSessionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@AppScope
@Inject
class FCMRepository(private val sessionRepository: SessionRepository, private val fcmApi: FCMApi) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        handleSessionState()
    }

    private fun handleSessionState() = coroutineScope.launch {
        sessionRepository.state.collectLatest {
            if (it is UserSessionState.ActiveSession.Authorized)
                sessionRepository.fcmToken?.let { token ->
                    repeat(5) {
                        try {
                            fcmApi.updateToken(UpdateFCMTokenRequest(token))
                            return@collectLatest
                        } catch (ex: Exception) {
                            Log.e("fcm", ex.stackTraceToString())
                            delay(5000L)
                        }
                    }
                }
        }
    }
}
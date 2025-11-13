package io.github.zidbrain.fchat

import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.github.zidbrain.fchat.android.ui.main.FCHAT_MESSAGES
import io.github.zidbrain.fchat.common.di.SessionComponent
import io.github.zidbrain.fchat.common.host.repository.UserSessionState
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.random.Random

class ChatFCMService : FirebaseMessagingService() {

    private lateinit var component: SessionComponent

    private val storage by lazy { component.encryptedStorage }
    private val chatRepository by lazy { component.chatRepository }
    private val sessionRepository by lazy { component.sessionRepository }
    private val loginRepository by lazy { component.loginRepository }

    private val json = Json.Default

    override fun onCreate() {
        component = TODO()
        super.onCreate()
    }

    override fun onNewToken(token: String) {
        storage.fcmToken = token
    }

    override fun onMessageReceived(message: RemoteMessage) = runBlocking {
        Log.i("Message", message.toIntent().toString())
        val content = message.data["content"] ?: return@runBlocking

        if (sessionRepository.state.value is UserSessionState.ActiveSession.Unauthorized) {
            loginRepository.requestAccessToken()
        }
        val decryptedMessage = chatRepository.message(json.decodeFromString(content))

        val notificationManager = NotificationManagerCompat.from(baseContext)
        if (notificationManager.areNotificationsEnabled()) {
            val notification = NotificationCompat.Builder(baseContext, FCHAT_MESSAGES)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setSmallIcon(io.github.zidbrain.fchat.android.R.drawable.outline_chat_24)
                .setContentTitle(decryptedMessage.sender.name)
                .setContentText(decryptedMessage.content)
                .build()
            notificationManager.notify(Random.nextInt(), notification)
        }
    }
}
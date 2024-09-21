package io.github.zidbrain.fchat.common.chat.api

import io.github.zidbrain.fchat.common.conversation.api.dto.WebSocketMessageIn
import io.github.zidbrain.fchat.common.conversation.api.dto.WebSocketMessageOut
import io.github.zidbrain.fchat.common.host.repository.SessionRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocketSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatApi(
    private val client: HttpClient,
    private val sessionRepository: SessionRepository
) {
    private suspend fun DefaultClientWebSocketSession.authenticate(accessToken: String) {
        sendSerialized(accessToken)
    }

    private var session: DefaultClientWebSocketSession? = null

    suspend fun send(payload: WebSocketMessageOut.RequestPayload) = withContext(Dispatchers.IO) {
        session?.let {
            it.sendSerialized(WebSocketMessageOut.Request(payload))
            it.receiveDeserialized<WebSocketMessageIn.Ok>()
        } ?: throw IllegalStateException("Session died")
    }

    suspend fun connect(messageHandler: suspend (WebSocketMessageIn.ContentPayload) -> Unit) {
        val originalContext = currentCoroutineContext()
        withContext(Dispatchers.IO) {
            session = client.webSocketSession("chat").apply {
                authenticate(sessionRepository.accessToken)
                launch {
                    while (true) {
                        val content = receiveDeserialized<WebSocketMessageIn.Content>()
                        withContext(originalContext) { messageHandler(content.payload) }
                        sendSerialized(WebSocketMessageOut.Ok)
                    }
                }
            }
        }
    }
}
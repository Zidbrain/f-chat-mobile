package io.github.zidbrain.fchat.common.chat.api

import io.github.zidbrain.fchat.common.conversation.api.dto.ChatSocketMessageIn
import io.github.zidbrain.fchat.common.conversation.api.dto.ChatSocketMessageInContent
import io.github.zidbrain.fchat.common.conversation.api.dto.ChatSocketMessageOut
import io.github.zidbrain.fchat.common.conversation.api.dto.ChatSocketMessageOutContent
import io.github.zidbrain.fchat.common.di.CommonQualifiers
import io.github.zidbrain.fchat.common.host.repository.SessionRepository
import io.github.zidbrain.fchat.common.util.randomUUID
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.URLBuilder
import io.ktor.http.encodedPath
import kotlinx.coroutines.*
import org.koin.core.annotation.Qualifier
import org.koin.core.annotation.Single
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.seconds

@Single
class ChatApi(
    @Qualifier(CommonQualifiers.Unauthorized::class)
    private val client: HttpClient,
    @Qualifier(CommonQualifiers.HostUrl::class)
    private val hostUrlString: String,
    private val sessionRepository: SessionRepository
) {
    private suspend fun DefaultClientWebSocketSession.authenticate(accessToken: String) {
        sendSerialized(accessToken)
    }

    private var session: DefaultClientWebSocketSession? = null

    private val requests =
        mutableMapOf<String, CancellableContinuation<ChatSocketMessageInContent.Control>>()

    suspend fun send(content: ChatSocketMessageOutContent): ChatSocketMessageInContent.Control =
        withTimeout(5.seconds) {
            val request = ChatSocketMessageOut(
                socketMessageId = randomUUID(),
                content = content
            )
            session?.sendSerialized(request) ?: throw IllegalStateException("Session died")

            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    requests.remove(request.socketMessageId)
                }
                requests[request.socketMessageId] = continuation
            }
        }

    private suspend fun DefaultClientWebSocketSession.handleConnection(
        messageHandler: suspend (ChatSocketMessageInContent.Payload) -> ChatSocketMessageOutContent.Control
    ): Nothing {
        while (true) {
            val message = receiveDeserialized<ChatSocketMessageIn>()
            when (val content = message.content) {
                is ChatSocketMessageInContent.Control -> requests[message.socketMessageId]?.let {
                    it.resumeWith(Result.success(content))
                    requests.remove(message.socketMessageId)
                } ?: throw IllegalStateException("Got response for unknown request")

                is ChatSocketMessageInContent.Payload -> launch {
                    withTimeout(5.seconds) {
                        val result = messageHandler(content)
                        val response = ChatSocketMessageOut(
                            socketMessageId = message.socketMessageId,
                            content = result
                        )

                        sendSerialized(response)
                    }
                }
            }
        }
    }

    suspend fun connect(
        onConnectionEstablished: suspend () -> Unit,
        messageHandler: suspend (ChatSocketMessageInContent.Payload) -> ChatSocketMessageOutContent.Control
    ): Nothing {
        val url = URLBuilder("${hostUrlString}/chat")
        session = client.webSocketSession(host = url.host, port = url.port, path = url.encodedPath)
            .apply {
                authenticate(sessionRepository.accessToken)
            }

        coroutineScope {
            launch { onConnectionEstablished() }
            try {
                session!!.handleConnection(messageHandler)
            } catch (ex: Exception) {
                if (ex is CancellationException) {
                    requests.forEach { (_, cont) -> cont.cancel(ex.cause) }
                    requests.clear()
                    throw ex.cause ?: ex
                }
                requests.forEach { (_, cont) -> cont.resumeWithException(ex) }
                requests.clear()
                throw ex
            } finally {
                session = null
            }
        }
    }
}
package io.github.zidbrain.fchat.common.chat.model

import io.github.zidbrain.fchat.common.user.model.User
import kotlin.time.Instant

data class ChatMessage(
    val id: String,
    val status: MessageStatus,
    val sender: User,
    val content: String,
    val sentAt: Instant
)

enum class MessageStatus {
    Initial, Delivered, Read, NotDelivered
}
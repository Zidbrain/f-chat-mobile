package io.github.zidbrain.fchat.common.nav

import kotlinx.serialization.Serializable

@Serializable
sealed class ConversationNavigationInfo {
    @Serializable
    data class ConversationId(val id: String) : ConversationNavigationInfo()
    @Serializable
    data class NewDirectMessageConversation(val withUserId: String) : ConversationNavigationInfo()
}
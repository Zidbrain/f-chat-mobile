package io.github.zidbrain.fchat.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.zidbrain.fchat.android.ui.conversation.ConversationScreen
import io.github.zidbrain.fchat.android.ui.main.MainScreen
import io.github.zidbrain.fchat.common.nav.ConversationNavigationInfo
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun FChatNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Destination.Main::class
    ) {
        composable<Destination.Main> {
            MainScreen(
                onNavigateToConversation = {
                    navController.navigate(Destination.Conversation.fromNavInfo(it))
                }
            )
        }
        composable<Destination.Conversation> {
            val destination = it.toRoute<Destination.Conversation>()
            ConversationScreen(
                viewModel = koinViewModel { parametersOf(destination.toNavInfo()) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Serializable
sealed class Destination {
    @Serializable
    data object Main : Destination()

    @Serializable
    data class Conversation(val conversationId: String? = null, val userId: String? = null) {
        fun toNavInfo(): ConversationNavigationInfo {
            if (conversationId != null)
                return ConversationNavigationInfo.ConversationId(conversationId)
            else if (userId != null)
                return ConversationNavigationInfo.NewDirectMessageConversation(userId)
            throw IllegalStateException()
        }

        companion object {
            fun fromNavInfo(model: ConversationNavigationInfo): Conversation =
                when (model) {
                    is ConversationNavigationInfo.ConversationId -> Conversation(conversationId = model.id)
                    is ConversationNavigationInfo.NewDirectMessageConversation -> Conversation(
                        userId = model.withUserId
                    )
                }
        }
    }
}
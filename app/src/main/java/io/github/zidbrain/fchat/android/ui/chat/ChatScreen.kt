package io.github.zidbrain.fchat.android.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import io.github.zidbrain.fchat.android.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(onMenuClicked: () -> Unit) = Surface(modifier = Modifier.fillMaxSize()) {
    Column {
        CenterAlignedTopAppBar(
            navigationIcon = {
                IconButton(onClick = onMenuClicked) {
                    Icon(
                        painter = painterResource(R.drawable.outline_menu_24),
                        contentDescription = null
                    )
                }
            },
            title = { Text("F Chat") },
            actions = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        painter = painterResource(R.drawable.outline_search_24),
                        contentDescription = null
                    )
                }
            }
        )
    }
}

@Composable
@Preview
fun ChatPreview() {
    ChatScreen {

    }
}
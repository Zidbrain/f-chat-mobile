package io.github.zidbrain.fchat.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.zidbrain.fchat.android.ui.host.HostScreen
import io.github.zidbrain.fchat.android.ui.theme.FChatMobileTheme
import org.koin.compose.KoinContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KoinContext {
                FChatMobileTheme {
                    HostScreen()
                }
            }
        }
    }
}
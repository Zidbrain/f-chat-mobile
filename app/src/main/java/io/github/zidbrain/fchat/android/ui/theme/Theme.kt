package io.github.zidbrain.fchat.android.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat

@Composable
fun FChatTheme(
    isAppearanceLightSystemBars: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isAppearanceLightSystemBars
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isAppearanceLightSystemBars
        }
    }

    val typography = Typography(
        titleLarge = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium)
    )

    MaterialTheme(
        typography = typography,
        content = content
    )
}
package io.github.zidbrain.fchat.android.ui.common

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun SystemBarsAppearance(
    isAppearanceLightStatusBars: Boolean = isSystemInDarkTheme(),
    isAppearanceLightNavigationBars: Boolean = isSystemInDarkTheme()
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isAppearanceLightStatusBars
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = isAppearanceLightNavigationBars
        }
    }
}
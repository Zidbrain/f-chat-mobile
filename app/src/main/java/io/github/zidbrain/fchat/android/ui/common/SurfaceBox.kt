package io.github.zidbrain.fchat.android.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
inline fun SurfaceBox(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    propagateMinConstraints: Boolean = false,
    crossinline content: @Composable BoxScope.() -> Unit
) =
    Surface(modifier = modifier) {
        Box(
            contentAlignment = contentAlignment,
            propagateMinConstraints = propagateMinConstraints
        ) {
            content()
        }
    }
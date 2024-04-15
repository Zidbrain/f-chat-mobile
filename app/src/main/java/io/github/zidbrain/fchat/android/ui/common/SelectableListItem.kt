package io.github.zidbrain.fchat.android.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SelectableListItem(
    modifier: Modifier = Modifier,
    colors: SelectableListItemColors = SelectableListItemDefaults.colors(),
    selected: Boolean,
    content: @Composable RowScope.() -> Unit
) {
    val backgroundColor by colors.getColor(selected = selected)
    Row(
        modifier = modifier
            .background(backgroundColor)
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 10.dp)
    ) {
        content()
    }
}

@Immutable
data class SelectableListItemColors(
    val selectedColor: Color,
    val defaultColor: Color
) {
    @Composable
    fun getColor(selected: Boolean): State<Color> = animateColorAsState(
        targetValue = if (selected) selectedColor else defaultColor,
        label = "color anim"
    )
}

object SelectableListItemDefaults {
    @Composable
    fun colors(
        selectedColor: Color = MaterialTheme.colorScheme.secondaryContainer,
        defaultColor: Color = Color(255, 255, 255, 0)
    ) = SelectableListItemColors(selectedColor, defaultColor)
}
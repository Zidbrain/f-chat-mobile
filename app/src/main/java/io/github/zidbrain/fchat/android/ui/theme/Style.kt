package io.github.zidbrain.fchat.android.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object Style {
    val Logo = generateTextStyle() + TextStyle(
        fontWeight = FontWeight.W600,
        fontSize = 56.sp
    )
    val LargeBold = generateTextStyle() + TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    )
    val Regular = generateTextStyle() + TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )

    private fun generateTextStyle() = TextStyle(
        fontFamily = FontFamily.Default
    )
}
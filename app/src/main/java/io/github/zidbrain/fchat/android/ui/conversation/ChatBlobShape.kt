package io.github.zidbrain.fchat.android.ui.conversation

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

@Suppress("FunctionName")
internal fun ChatBlobShape(
    density: Density,
    cornerRadius: Dp,
    arrowSize: Dp,
    reverse: Boolean
): Shape = with(density) {
    val cornerRadiusPx = cornerRadius.toPx()
    val arrowSizePx = arrowSize.toPx()

    return GenericShape { size, _ ->
        val cr = CornerRadius(cornerRadiusPx)
        addRoundRect(
            RoundRect(
                rect = Rect(
                    offset = Offset(0f, 0f),
                    size = Size(size.width, size.height)
                ),
                topLeft = cr,
                topRight = cr,
                bottomLeft = if (reverse) cr else CornerRadius.Zero,
                bottomRight = if (reverse) CornerRadius.Zero else cr
            )
        )

        if (reverse) {
            moveTo(size.width, size.height)
            lineTo(size.width + arrowSizePx / 2, size.height)
            lineTo(size.width, size.height - arrowSizePx)
        } else {
            moveTo(0f, size.height)
            lineTo(-arrowSizePx / 2, size.height)
            lineTo(0f, size.height - arrowSizePx)
        }
    }
}
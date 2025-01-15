package io.github.zidbrain.fchat.android.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.zidbrain.fchat.android.R

@Composable
fun RoundedIcon(
    iconUrl: String,
    imageSize: Dp = RoundedIconDefaults.ImageSize,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .background(color = MaterialTheme.colorScheme.background, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.outline_person_24),
            contentDescription = null,
            modifier = Modifier.size(imageSize)
        )
    }
}

object RoundedIconDefaults {
    val ImageSize = 40.dp
}

@Preview
@Composable
private fun RoundedIconPreview() {
    RoundedIcon("icon")
}
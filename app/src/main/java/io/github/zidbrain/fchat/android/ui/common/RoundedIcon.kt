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
import androidx.compose.ui.unit.dp
import io.github.zidbrain.fchat.android.R

@Composable
fun RoundedIcon(iconUrl: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .background(color = MaterialTheme.colorScheme.background, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(painterResource(R.drawable.outline_person_24), null, modifier = Modifier.size(40.dp))
    }
}

@Preview
@Composable
private fun RoundedIconPreview() {
    RoundedIcon("icon")
}
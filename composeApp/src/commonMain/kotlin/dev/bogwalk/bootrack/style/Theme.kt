package dev.bogwalk.bootrack.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable

@Composable
fun BTTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BTColors,
        typography = BTTypography,
        shapes = BTShapes
    ) {
        Surface(content = content)
    }
}

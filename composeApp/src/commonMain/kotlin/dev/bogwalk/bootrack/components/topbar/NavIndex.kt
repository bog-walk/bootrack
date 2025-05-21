package dev.bogwalk.bootrack.components.topbar

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ChevronLeft
import androidx.compose.material.icons.sharp.ChevronRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import dev.bogwalk.bootrack.components.buttons.TintedIconButton
import dev.bogwalk.bootrack.components.utils.navigationButtonColors

@Composable
internal fun NavIndex(
    index: Int,
    count: Int,
    onPreviousRequest: () -> Unit,
    onNextRequest: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        TintedIconButton(
            onClick = onPreviousRequest,
            icon = Icons.Sharp.ChevronLeft,
            enabled = index != 1,
            colors = navigationButtonColors()
        )
        Text(
            text = "$index of $count",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge
        )
        TintedIconButton(
            onClick = onNextRequest,
            icon = Icons.Sharp.ChevronRight,
            enabled = index != count,
            colors = navigationButtonColors()
        )
    }
}

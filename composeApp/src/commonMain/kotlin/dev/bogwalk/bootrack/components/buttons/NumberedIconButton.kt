package dev.bogwalk.bootrack.components.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.material3.IconToggleButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import dev.bogwalk.bootrack.components.utils.displayButtonColors
import dev.bogwalk.bootrack.components.utils.toggleButtonColors
import dev.bogwalk.bootrack.style.iconSizeSmall
import dev.bogwalk.bootrack.style.paddingSmall

@Composable
internal fun NumberedIconButton(
    onClick: () -> Unit,
    count: Int,
    icon: ImageVector,
    iconDescription: String = "",
    modifier: Modifier = Modifier,
    tooltipText: String? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
        ,
        horizontalArrangement = Arrangement.spacedBy(-paddingSmall, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Text(
            text = count.takeIf { it != 0 }?.toString() ?: "",
            modifier = Modifier.requiredWidthIn(min = iconSizeSmall),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodyMedium
        )
        TintedIconButton(
            onClick = onClick,
            icon = icon,
            iconDescription = iconDescription,
            smallerIcon = true,
            enabled = enabled && count != 0,
            tooltipText = tooltipText,
            colors = displayButtonColors()
        )
    }
}

@Composable
internal fun NumberedToggleButton(
    onToggle: (Boolean) -> Unit,
    checked: Boolean,
    count: Int,
    icon: ImageVector,
    iconDescription: String = "",
    modifier: Modifier = Modifier,
    tooltipText: String? = null,
    colors: IconToggleButtonColors = toggleButtonColors(MaterialTheme.colorScheme.tertiary),
) {
    Row(
        modifier = modifier
        ,
        horizontalArrangement = Arrangement.spacedBy(-paddingSmall, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Text(
            text = count.takeIf { it != 0 }?.toString() ?: "",
            modifier = Modifier.requiredWidthIn(min = iconSizeSmall),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodyMedium
        )
        TintedToggleButton(
            onToggle = onToggle,
            checked = checked,
            icon = icon,
            iconDescription = iconDescription,
            smallerIcon = true,
            tooltipText = tooltipText,
            colors = colors,
        )
    }
}

package dev.bogwalk.bootrack.components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.vector.ImageVector
import dev.bogwalk.bootrack.components.utils.drawBorderWithShadow
import dev.bogwalk.bootrack.components.utils.navigationButtonColors
import dev.bogwalk.bootrack.style.iconContainerSize
import dev.bogwalk.bootrack.style.iconSize

@Composable
internal fun ElevatedIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    iconDescription: String = "",
    modifier: Modifier = Modifier,
    colors: IconButtonColors = navigationButtonColors(),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    NoIntentionButton(
        onClick = onClick,
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                drawBorderWithShadow(lighter = true)
            }
            .requiredSize(iconContainerSize)
        ,
        interactionSource = interactionSource
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconDescription,
            modifier = Modifier.requiredSize(iconSize),
            tint = if (isHovered) colors.containerColor else colors.contentColor
        )
    }
}

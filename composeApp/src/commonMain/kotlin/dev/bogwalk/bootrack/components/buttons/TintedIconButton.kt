package dev.bogwalk.bootrack.components.buttons

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconToggleButtonColors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import dev.bogwalk.bootrack.components.utils.actionButtonColors
import dev.bogwalk.bootrack.components.utils.toggleButtonColors
import dev.bogwalk.bootrack.style.*

@Composable
internal fun TintedIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    iconDescription: String = "",
    smallerIcon: Boolean = false,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tooltipText: String? = null,
    colors: IconButtonColors = actionButtonColors(),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    TooltipButton(
        onClick = onClick,
        modifier = modifier
            .requiredSize(if (smallerIcon) iconContainerSizeSmall else iconContainerSize)
        ,
        enabled = enabled,
        tooltipText = tooltipText,
        interactionSource = interactionSource
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconDescription,
            modifier = Modifier
                .requiredSize(if (smallerIcon) iconSizeSmall else iconSize)
            ,
            tint = when {
                isHovered -> colors.containerColor
                enabled -> colors.contentColor
                else -> colors.disabledContentColor
            }
        )
    }
}

@Composable
internal fun TintedToggleButton(
    onToggle: (Boolean) -> Unit,
    checked: Boolean,
    icon: ImageVector,
    iconDescription: String = "",
    smallerIcon: Boolean = false,
    modifier: Modifier = Modifier,
    tooltipText: String? = null,
    colors: IconToggleButtonColors = toggleButtonColors(neonYellow),
) {
    var checked by remember { mutableStateOf(checked) }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    TooltipButton(
        onClick = {
            checked = !checked
            onToggle(checked)
        },
        modifier = modifier
            .requiredSize(if (smallerIcon) iconContainerSizeSmall else iconContainerSize)
        ,
        enabled = true,
        tooltipText = tooltipText,
        interactionSource = interactionSource
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconDescription,
            modifier = Modifier
                .requiredSize(if (smallerIcon) iconSizeSmall else iconSize)
            ,
            tint = when {
                checked && isHovered -> colors.checkedContainerColor
                checked -> colors.checkedContentColor
                isHovered -> colors.containerColor
                else -> colors.contentColor
            }
        )
    }
}

package dev.bogwalk.bootrack.components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.StrokeCap
import dev.bogwalk.bootrack.components.utils.drawBorder
import dev.bogwalk.bootrack.style.*

@Composable
internal fun FilledActionButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tooltipText: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    TooltipButton(
        onClick = onClick,
        modifier = modifier
            .padding(paddingSmall)
            .background(
                when {
                    enabled && isHovered -> lightPink
                    enabled -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.background
                }
            )
            .drawBehind {
                val borderColor = when {
                    enabled && isHovered -> neonPink
                    enabled -> shadowPink
                    else -> lightGrey
                }
                drawBorder(borderSmall.toPx(), 0f, borderColor, StrokeCap.Butt)
            }
            .requiredSize(actionButtonWidth, actionButtonHeight)
        ,
        enabled = enabled,
        tooltipText = if (enabled) null else tooltipText,
        interactionSource = interactionSource,
    ) {
        Text(
            text = text,
            color = if (enabled) MaterialTheme.colorScheme.inverseOnSurface else lightGrey,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

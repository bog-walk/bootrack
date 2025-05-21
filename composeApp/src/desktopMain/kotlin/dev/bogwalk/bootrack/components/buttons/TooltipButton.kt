package dev.bogwalk.bootrack.components.buttons

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.bogwalk.bootrack.style.paddingMedium
import dev.bogwalk.bootrack.style.paddingSmall

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal actual fun TooltipButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    tooltipText: String?,
    interactionSource: MutableInteractionSource?,
    content: @Composable (BoxScope.() -> Unit)
) {
    if (tooltipText != null) {
        TooltipArea(
            tooltip = {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.9f),
                    shadowElevation = paddingMedium,
                ) {
                    Text(
                        text = tooltipText,
                        modifier = Modifier.padding(paddingSmall),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            delayMillis = 200,
            tooltipPlacement = TooltipPlacement.ComponentRect(
                alignment = Alignment.BottomEnd,
            )
        ) {
            NoIntentionButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                interactionSource = interactionSource,
                content = content
            )
        }
    } else {
        NoIntentionButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            interactionSource = interactionSource,
            content = content
        )
    }
}

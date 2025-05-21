package dev.bogwalk.bootrack.components.navbar

import androidx.compose.animation.core.Transition
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import dev.bogwalk.bootrack.screens.NavBarState
import dev.bogwalk.bootrack.components.buttons.NoIntentionButton
import dev.bogwalk.bootrack.components.utils.drawNavButtonBorder
import dev.bogwalk.bootrack.style.paddingMedium

@Composable
internal fun NavButton(
    onClick: () -> Unit,
    transition: Transition<NavBarState>,
    label: String,
    icon: Painter,
    iconDescription: String = "",
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    NoIntentionButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = paddingMedium, top = paddingMedium, end = paddingMedium)
            .background(
                if (isHovered) {
                    MaterialTheme.colorScheme.background.copy(alpha = 0.7f)
                } else {
                    Color.Transparent
                }
            )
            .drawBehind {
                drawNavButtonBorder(isHovered)
            }
        ,
        enabled = enabled,
        interactionSource = interactionSource
    ) {
        NavItem(
            transition = transition,
            label = label,
            icon = icon,
            iconDescription = iconDescription,
            modifier = Modifier.align(Alignment.CenterStart),
        )
    }
}

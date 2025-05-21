package dev.bogwalk.bootrack.components.navbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Transition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import dev.bogwalk.bootrack.components.utils.visibilityEntry
import dev.bogwalk.bootrack.components.utils.visibilityExit
import dev.bogwalk.bootrack.screens.NavBarState
import dev.bogwalk.bootrack.style.iconContainerSize
import dev.bogwalk.bootrack.style.paddingMedium
import dev.bogwalk.bootrack.style.paddingSmall

@Composable
internal fun NavItem(
    transition: Transition<NavBarState>,
    label: String,
    icon: Painter,
    iconDescription: String = "",
    labelStyle: TextStyle = MaterialTheme.typography.labelLarge,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
        ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            painter = icon,
            contentDescription = iconDescription,
            modifier = Modifier
                .padding(start = paddingSmall / 2 + paddingSmall, end = paddingSmall, top = paddingMedium, bottom = paddingMedium)
                .requiredSize(iconContainerSize)
                .clip(CircleShape)
            ,
            tint = Color.Unspecified
        )
        transition.AnimatedVisibility(
            visible = { it == NavBarState.EXPANDED },
            enter = visibilityEntry(),
            exit = visibilityExit()
        ) {
            Text(
                text = label,
                modifier = Modifier
                    .padding(start = paddingSmall)
                ,
                color = MaterialTheme.colorScheme.onSurface,
                style = labelStyle
            )
        }
    }
}

package dev.bogwalk.bootrack.screens

import androidx.compose.animation.core.Transition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import dev.bogwalk.bootrack.components.navbar.NavButton
import dev.bogwalk.bootrack.components.notification.NotificationItem
import dev.bogwalk.bootrack.components.utils.drawDialogBorder
import dev.bogwalk.bootrack.model.SessionNotification
import dev.bogwalk.bootrack.style.navBarWide
import dev.bogwalk.bootrack.style.paddingMedium
import dev.bogwalk.bootrack.style.paddingSmall

@Composable
internal fun NavPopup(
    transition: Transition<NavBarState>,
    label: String,
    notifications: List<SessionNotification>,
    icon: Painter,
    enabled: Boolean,
    iconDescription: String = "",
    modifier: Modifier = Modifier,
    onToggleRead: (SessionNotification) -> Unit,
    onShowIssue: (String) -> Unit,
) {
    var showPopup by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
        ,
    ) {
        NavButton(
            onClick = { showPopup = !showPopup },
            transition = transition,
            label = label,
            icon = icon,
            iconDescription = iconDescription,
            enabled = enabled,
        )

        if (showPopup) {
            Popup(
                alignment = Alignment.BottomStart,
                offset = IntOffset(x = this.constraints.maxWidth, y = 0),
                onDismissRequest = { showPopup = false },
                properties = PopupProperties(
                    focusable = true,
                    clippingEnabled = false,
                )
            ) {
                NotificationList(
                    notifications = notifications,
                    onToggleRead = onToggleRead,
                    onShowIssue = onShowIssue,
                )
            }
        }
    }
}

@Composable
internal fun NotificationList(
    notifications: List<SessionNotification>,
    onToggleRead: (SessionNotification) -> Unit,
    onShowIssue: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                drawDialogBorder()
            }
            .padding(start = paddingSmall, top = paddingSmall, end = paddingMedium, bottom = paddingSmall)
            .requiredHeightIn(max = navBarWide)
            .verticalScroll(rememberScrollState())
        ,
    ) {
        notifications.forEach { notification ->
            NotificationItem(
                notification = notification,
                onToggleRead = onToggleRead,
                onShowIssue = onShowIssue,
            )
        }
    }
}

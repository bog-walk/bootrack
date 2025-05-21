package dev.bogwalk.bootrack.components.notification

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.*
import bootrack.composeapp.generated.resources.Res
import bootrack.composeapp.generated.resources.circle
import dev.bogwalk.bootrack.components.buttons.NoIntentionButton
import dev.bogwalk.bootrack.components.utils.iconOptions
import dev.bogwalk.bootrack.components.utils.tintOptions
import dev.bogwalk.bootrack.model.SessionNotification
import dev.bogwalk.bootrack.style.*
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun NotificationItem(
    notification: SessionNotification,
    onToggleRead: (SessionNotification) -> Unit,
    onShowIssue: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(vertical = paddingSmall)
            .requiredWidth(notificationBoxWidth)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = iconOptions[notification.senderAvatar],
            contentDescription = "",
            modifier = Modifier
                .padding(horizontal = paddingMedium)
                .requiredSize(iconContainerSize)
                .clip(CircleShape),
            tint = tintOptions[notification.senderAvatarTint]
        )

        Text(
            text = buildAnnotatedString {
                withLink(
                    link = LinkAnnotation.Clickable(
                        tag = "sender",
                        styles = getNotificationLinkStyles(notification.isRead),
                        linkInteractionListener = {}
                    )
                ) {
                    append(notification.senderName)
                }
                withStyle(
                    style = getNotificationLinkStyles(notification.isRead).style ?: error("Issue getting link style")
                ) {
                    append(notification.message)
                }
                withLink(
                    link = LinkAnnotation.Clickable(
                        tag = "issue",
                        styles = getNotificationLinkStyles(notification.isRead),
                        linkInteractionListener = { onShowIssue(notification.issueCode) }
                    )
                ) {
                    append(notification.issueCode)
                }
            },
            modifier = Modifier
                .padding(end = paddingSmall)
                .weight(1f)
            ,
        )

        NoIntentionButton(
            onClick = { onToggleRead(notification.copy(isRead = !notification.isRead)) },
            modifier = Modifier
                .requiredSize(iconContainerSizeSmall)
                .pointerHoverIcon(PointerIcon.Hand)
        ) {
            Icon(
                painter = painterResource(Res.drawable.circle),
                contentDescription = "",
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = paddingMedium)
                    .requiredSize(paddingMedium),
                tint = if (notification.isRead) lightGrey else neonRed
            )
        }
    }
}

@Composable
private fun getNotificationLinkStyles(isRead: Boolean): TextLinkStyles = TextLinkStyles(
    style = SpanStyle(
        color = if (isRead) lightGrey else MaterialTheme.colorScheme.onSurface,
        fontSize = MaterialTheme.typography.titleSmall.fontSize,
        fontWeight = MaterialTheme.typography.titleSmall.fontWeight,
        fontFamily = MaterialTheme.typography.titleSmall.fontFamily,
    ),
    hoveredStyle = SpanStyle(
        color = if (isRead) lightGrey else MaterialTheme.colorScheme.tertiary,
        fontSize = MaterialTheme.typography.titleSmall.fontSize,
        fontWeight = MaterialTheme.typography.titleSmall.fontWeight,
        fontFamily = MaterialTheme.typography.titleSmall.fontFamily,
    ),
)

package dev.bogwalk.bootrack.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.bogwalk.bootrack.components.buttons.TooltipButton
import dev.bogwalk.bootrack.model.IssueSummarized
import dev.bogwalk.bootrack.model.Location
import dev.bogwalk.bootrack.style.*
import kotlin.math.absoluteValue
import kotlin.math.truncate

@Composable
fun IssueRadar(
    icon: ImageVector,
    tint: Color,
    userLocation: Location,
    issues: List<IssueSummarized>,
    onShowIssueRequest: (IssueSummarized) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = paddingSmall)
        ,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "",
            modifier = Modifier
                .padding(horizontal = paddingSmall, vertical = paddingMedium)
                .requiredSize(iconContainerSize)
                .clip(CircleShape)
                .align(Alignment.Center)
            ,
            tint = tint
        )

        issues
            .takeUnless { it.isEmpty() }
            ?.mapNotNull { it.issue.location?.getOffsets(userLocation) }
            ?.normalized(maxHeight - iconContainerSize, maxWidth - iconContainerSize)
            ?.zip(issues)
            ?.forEach { (offset, issue) ->
                RadarPoint(
                    issue = issue,
                    yOffset = offset.yOffset.dp,
                    xOffset = offset.xOffset.dp,
                    onShowIssue = onShowIssueRequest
                )
            }
    }
}

private fun Location.getOffsets(target: Location): LocationOffset {
    val yDiff = truncate(target.latitude - this.latitude)
    val xDiff = truncate(target.longitude - this.longitude) * -1
    return LocationOffset(yDiff, xDiff)
}

private data class LocationOffset(val yOffset: Double, val xOffset: Double)

private fun List<LocationOffset>.normalized(
    maxHeight: Dp,
    maxWidth: Dp,
): List<LocationOffset> {
    val maxYOffset = this.maxOf { it.yOffset.absoluteValue }
    val maxXOffset = this.maxOf { it.xOffset.absoluteValue }

    val centerY = truncate(maxHeight.value / 2)
    val centerX = truncate(maxWidth.value / 2)

    val yNormalized = truncate(centerY / maxYOffset)
    val xNormalized = truncate(centerX / maxXOffset)

    return this.map {
        val newYOffset = it.yOffset * yNormalized + centerY
        val newXOffset = it.xOffset * xNormalized + centerX
        LocationOffset(newYOffset, newXOffset)
    }
}

@Composable
private fun RadarPoint(
    issue: IssueSummarized,
    yOffset: Dp,
    xOffset: Dp,
    onShowIssue: (IssueSummarized) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .offset(x = xOffset, y = yOffset)
    ) {
        TooltipButton(
            onClick = { onShowIssue(issue) },
            modifier = Modifier
                .padding(horizontal = paddingSmall, vertical = paddingMedium)
            ,
            tooltipText = issue.issue.code,
            interactionSource = interactionSource
        ) {
            Text(
                text = " ",
                modifier = Modifier
                    .requiredSize(iconSizeSmall)
                    .clip(CircleShape)
                    .background(
                        if (isHovered) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
            )
        }
    }
}

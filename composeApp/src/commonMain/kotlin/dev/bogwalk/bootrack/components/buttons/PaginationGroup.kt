package dev.bogwalk.bootrack.components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ChevronLeft
import androidx.compose.material.icons.sharp.ChevronRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import bootrack.composeapp.generated.resources.Res
import bootrack.composeapp.generated.resources.next_page
import bootrack.composeapp.generated.resources.previous_page
import dev.bogwalk.bootrack.components.utils.drawBorder
import dev.bogwalk.bootrack.components.utils.drawPaginationBorder
import dev.bogwalk.bootrack.components.utils.navigationButtonColors
import dev.bogwalk.bootrack.style.*
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max
import kotlin.math.min

@Composable
internal fun PaginationGroup(
    onChangePageRequest: (Int) -> Unit,
    pageIndex: Int,
    pageCount: Int,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableStateOf(pageIndex) }

    Column(
        modifier = modifier
            .width(IntrinsicSize.Max)
        ,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
            ,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TintedIconButton(
                onClick = { onChangePageRequest(pageIndex - 1) },
                icon = Icons.Sharp.ChevronLeft,
                enabled = pageIndex != 1,
                colors = navigationButtonColors().copy(contentColor = MaterialTheme.colorScheme.tertiary)
            )
            Text(
                text = stringResource(Res.string.previous_page),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.widthIn(min = iconContainerSize).weight(1f))
            Text(
                text = stringResource(Res.string.next_page),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge
            )
            TintedIconButton(
                onClick = { onChangePageRequest(pageIndex + 1) },
                icon = Icons.Sharp.ChevronRight,
                enabled = pageIndex != pageCount,
                colors = navigationButtonColors().copy(contentColor = MaterialTheme.colorScheme.tertiary)
            )
        }
        Row(
            modifier = Modifier
                .padding(top = paddingSmall)
                .background(MaterialTheme.colorScheme.background)
                .drawBehind {
                    drawBorder(borderSmall.toPx(), 0f, neonBlue, StrokeCap.Butt)
                }
            ,
        ) {
            val maxPages = 3

            val mod = if (pageIndex % maxPages == 0) 0 else 1
            val currentBatch = pageIndex / maxPages + mod
            val rangeEnd = min(max(maxPages + 1, pageCount), currentBatch * maxPages + 1)
            val rangeStart = (rangeEnd - maxPages).coerceAtLeast(1)

            for (i in rangeStart..rangeEnd) {
                PageButton(
                    onSelect = {
                        selected = i
                        onChangePageRequest(selected)
                    },
                    index = i,
                    selected = i == selected,
                    overflow = i == rangeEnd && i != pageCount,
                    filler = i > pageCount,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PageButton(
    onSelect: (Int) -> Unit,
    index: Int,
    selected: Boolean,
    overflow: Boolean,
    filler: Boolean,
    modifier: Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    NoIntentionButton(
        onClick = { onSelect(index) },
        modifier = modifier
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                } else {
                    Color.Transparent
                }
            )
            .drawBehind {
                if (selected) {
                    drawPaginationBorder(borderSmall.toPx(), 0f, neonBlue, StrokeCap.Butt)
                }
            }
            .heightIn(min = iconContainerSize)
        ,
        enabled = !selected && !filler,
        interactionSource = interactionSource,
    ) {
        Text(
            text = when {
                filler -> ""
                overflow -> "..."
                else -> index.toString()
            },
            color = when {
                selected -> MaterialTheme.colorScheme.onSurface
                isHovered -> MaterialTheme.colorScheme.tertiary
                else -> lightGrey
            },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

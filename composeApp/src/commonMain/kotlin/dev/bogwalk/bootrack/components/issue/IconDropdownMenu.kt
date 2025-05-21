package dev.bogwalk.bootrack.components.issue

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material.icons.sharp.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import dev.bogwalk.bootrack.components.buttons.TintedIconButton
import dev.bogwalk.bootrack.style.iconSizeSmall
import dev.bogwalk.bootrack.style.paddingMedium

@Composable
internal fun IconDropdownMenu(
    deleteText: String,
    modifier: Modifier = Modifier,
    onSelectDelete: () -> Unit,
) {
    var itemsExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
        ,
    ) {
        TintedIconButton(
            onClick = { itemsExpanded = !itemsExpanded },
            icon = Icons.Sharp.MoreHoriz,
            smallerIcon = true,
        )
        DropdownMenu(
            expanded = itemsExpanded,
            onDismissRequest = { itemsExpanded = false },
            shape = MaterialTheme.shapes.small,
            containerColor = MaterialTheme.colorScheme.background,
            shadowElevation = paddingMedium,
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = deleteText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                onClick = {
                    itemsExpanded = !itemsExpanded
                    onSelectDelete()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Sharp.Delete,
                        contentDescription = "",
                        modifier = Modifier
                            .requiredSize(iconSizeSmall)
                        ,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
}

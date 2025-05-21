package dev.bogwalk.bootrack.components.navbar

import androidx.compose.animation.core.Transition
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.DpOffset
import dev.bogwalk.bootrack.screens.NavBarState
import dev.bogwalk.bootrack.style.lightGrey
import dev.bogwalk.bootrack.style.paddingLarge
import dev.bogwalk.bootrack.style.paddingMedium

@Composable
internal fun <T> NavDropdownMenu(
    transition: Transition<NavBarState>,
    label: String,
    icon: Painter,
    menuItems: List<T>,
    currentItem: T,
    onMenuItemRequest: (T) -> Unit,
    getItemText: (T) -> String,
    iconDescription: String = "",
    modifier: Modifier = Modifier,
) {
    var itemsExpanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(currentItem) }

    BoxWithConstraints(
        modifier = modifier
        ,
    ) {
        NavButton(
            onClick = { itemsExpanded = !itemsExpanded },
            transition = transition,
            label = label,
            icon = icon,
            iconDescription = iconDescription
        )
        DropdownMenu(
            expanded = itemsExpanded,
            onDismissRequest = { itemsExpanded = false },
            modifier = Modifier
                .widthIn(min = this.maxWidth / 2)
                .pointerHoverIcon(icon = PointerIcon.Hand, overrideDescendants = true)
            ,
            offset = DpOffset(x = this.maxWidth / 2 + paddingLarge, y = -paddingMedium),
            shape = MaterialTheme.shapes.small,
            containerColor = MaterialTheme.colorScheme.background,
            shadowElevation = paddingMedium,
        ) {
            menuItems.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = getItemText(item),
                            color = if (selectedItem == item) MaterialTheme.colorScheme.tertiary else lightGrey,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    onClick = {
                        selectedItem = item
                        itemsExpanded = !itemsExpanded
                        onMenuItemRequest(item)
                    },
                )
            }
        }
    }
}

package dev.bogwalk.bootrack.components.user

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import dev.bogwalk.bootrack.style.lightGrey
import dev.bogwalk.bootrack.style.neonBlue
import dev.bogwalk.bootrack.style.paddingMedium
import dev.bogwalk.bootrack.style.searchBarMaxWidth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> DropDownSetting(
    label: String,
    menuItems: List<T>,
    currentItem: T,
    onMenuItemRequest: (T) -> Unit,
    getItemText: (T) -> String,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(currentItem) }

    BoxWithConstraints {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                modifier = Modifier
                    .requiredWidth(searchBarMaxWidth)
                ,
                style = MaterialTheme.typography.bodyMedium
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier
                    .pointerHoverIcon(icon = PointerIcon.Hand, overrideDescendants = true)
            ) {
                BasicTextField(
                    value = getItemText(selectedItem),
                    onValueChange = { selectedItem = menuItems.first { i -> getItemText(i) == it } },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    ,
                    readOnly = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = neonBlue),
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    matchTextFieldWidth = false,
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
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            onClick = {
                                if (selectedItem != item) {
                                    selectedItem = item
                                    onMenuItemRequest(item)
                                }
                                expanded = !expanded
                            },
                        )
                    }
                }
            }
        }
    }
}

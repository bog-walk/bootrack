package dev.bogwalk.bootrack.components.issue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import dev.bogwalk.bootrack.style.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> FieldDropDownItem(
    label: String,
    menuItems: List<T>,
    currentItem: T,
    onMenuItemRequest: (T) -> Unit,
    getItemText: (T) -> String,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    var expanded by remember { mutableStateOf(false) }

    BoxWithConstraints {
        Row(
            modifier = Modifier
                .fillMaxWidth()
            ,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
            ,
        ) {
            Column {
                Text(
                    text = label,
                    color = lightGrey,
                    modifier = Modifier
                        .padding(paddingSmall)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            enabled = enabled,
                            role = Role.DropdownList,
                            onClick = { expanded = !expanded },
                        )
                    ,
                    style = MaterialTheme.typography.titleSmall,
                )
                if (enabled) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        modifier = Modifier
                            .padding(paddingSmall)
                            .pointerHoverIcon(icon = PointerIcon.Hand, overrideDescendants = true)
                    ) {
                        BasicTextField(
                            value = getItemText(currentItem),
                            onValueChange = { },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            ,
                            readOnly = true,
                            textStyle = if (isHovered) {
                                MaterialTheme.typography.bodyMedium.copy(color = neonBlue)
                            } else {
                                MaterialTheme.typography.bodyMedium
                            },
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
                                            color = if (currentItem == item) MaterialTheme.colorScheme.tertiary else lightGrey,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    onClick = {
                                        if (currentItem != item) {
                                            onMenuItemRequest(item)
                                        }
                                        expanded = !expanded
                                    },
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = getItemText(currentItem),
                        modifier = Modifier
                            .padding(paddingSmall)
                        ,
                        style = if (isHovered) {
                            MaterialTheme.typography.bodyMedium.copy(color = neonBlue)
                        } else {
                            MaterialTheme.typography.bodyMedium
                        },
                    )
                }
            }
            
            if (icon != null && this@BoxWithConstraints.maxWidth >= fieldBoxCompactWidth) {
                icon()
            }
        }
    }
}

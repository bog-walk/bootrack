package dev.bogwalk.bootrack.components.buttons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import dev.bogwalk.bootrack.components.utils.textButtonColors

@Composable
internal fun InteractionTextButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    colors: ButtonColors = textButtonColors(),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Text(
        text = text,
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = true,
                role = Role.Button,
                onClick = onClick,
            )
        ,
        color = if (isHovered) colors.containerColor else colors.contentColor,
        style = style
    )
}

@Composable
internal fun InteractionTextPopup(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    colors: ButtonColors = textButtonColors(),
    popup: @Composable () -> Unit
) {
    var showPopup by remember { mutableStateOf(false) }
    var yOffset by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
        ,
    ) {
        InteractionTextButton(
            onClick = { showPopup = !showPopup },
            text = text,
            modifier = Modifier
                .onPlaced {
                    yOffset = it.size.height
                }
            ,
            style = style,
            colors = colors,
        )

        if (showPopup) {
            Popup(
                offset = IntOffset(x = 0, y = yOffset),
                onDismissRequest = { showPopup = false },
                content = popup
            )
        }
    }
}

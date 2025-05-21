package dev.bogwalk.bootrack.components.topbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material.icons.sharp.TaskAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import bootrack.composeapp.generated.resources.*
import dev.bogwalk.bootrack.components.buttons.TintedIconButton
import dev.bogwalk.bootrack.components.buttons.TintedToggleButton
import dev.bogwalk.bootrack.components.utils.TextFieldDefaultDecorationBox
import dev.bogwalk.bootrack.components.utils.toggleButtonColors
import dev.bogwalk.bootrack.components.utils.visibilityEntry
import dev.bogwalk.bootrack.components.utils.visibilityExit
import dev.bogwalk.bootrack.style.borderSmall
import dev.bogwalk.bootrack.style.iconContainerSize
import dev.bogwalk.bootrack.style.lightGrey
import dev.bogwalk.bootrack.style.paddingSmall
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MultiOptionSearchBar(
    transition: Transition<Boolean>,
    placeHolderText: String,
    filterResults: Boolean,
    modifier: Modifier = Modifier,
    onFilterRequest: (Boolean) -> Unit,
    onSearchRequest: (String) -> Unit,
    onCloseSearchRequest: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    var searchQuery by remember { mutableStateOf("") }
    val searchBorderColor by transition.animateColor(
        transitionSpec = { tween() },
        label = "search field border"
    ) { expanded ->
        if (expanded) MaterialTheme.colorScheme.primary else lightGrey
    }

    BasicTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        modifier = modifier
            .height(iconContainerSize)
            .padding(paddingSmall)
            .drawBehind {
                drawLine(searchBorderColor, Offset(0f, 0f), Offset(0f, size.height), borderSmall.toPx(), StrokeCap.Butt)
            }
        ,
        textStyle = MaterialTheme.typography.bodyMedium,
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        interactionSource = interactionSource
    ) { innerTextField ->
        TextFieldDefaultDecorationBox(
            text = searchQuery,
            innerTextField = innerTextField,
            singleLine = true,
            interactionSource = interactionSource,
            placeholder = {
                Text(
                    text = placeHolderText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = lightGrey
                )
            },
            trailingIcon = {
                Row(
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon.Default)
                    ,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TintedIconButton(
                        onClick = { onSearchRequest(searchQuery) },
                        icon = Icons.Sharp.Search,
                        smallerIcon = true,
                        enabled = searchQuery.isNotEmpty(),
                        tooltipText = if (searchQuery.isEmpty()) null else stringResource(Res.string.search_tt)
                    )
                    transition.AnimatedVisibility(
                        visible = { it },
                        enter = visibilityEntry(),
                        exit = visibilityExit()
                    ) {
                        TintedToggleButton(
                            onToggle = onFilterRequest,
                            checked = filterResults,
                            icon = Icons.Sharp.TaskAlt,
                            smallerIcon = true,
                            tooltipText = stringResource(
                                if (filterResults) Res.string.search_show_tt else Res.string.search_hide_tt
                            ),
                            colors = toggleButtonColors(
                                if (filterResults) MaterialTheme.colorScheme.primary else lightGrey
                            )
                        )
                    }
                    transition.AnimatedVisibility(
                        visible = { it },
                        enter = visibilityEntry(),
                        exit = visibilityExit()
                    ) {
                        TintedIconButton(
                            onClick = {
                                searchQuery = ""
                                onCloseSearchRequest()
                            },
                            icon = Icons.Sharp.Close,
                            smallerIcon = true,
                            tooltipText = stringResource(Res.string.search_clear_tt)
                        )
                    }
                }
            },
        )
    }
}

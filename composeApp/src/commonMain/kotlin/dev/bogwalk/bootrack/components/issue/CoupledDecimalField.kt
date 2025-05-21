package dev.bogwalk.bootrack.components.issue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import bootrack.composeapp.generated.resources.Res
import bootrack.composeapp.generated.resources.n_a
import dev.bogwalk.bootrack.components.buttons.TintedIconButton
import dev.bogwalk.bootrack.components.utils.TextFieldDefaultDecorationBox
import dev.bogwalk.bootrack.components.utils.actionButtonColors
import dev.bogwalk.bootrack.components.utils.drawBorder
import dev.bogwalk.bootrack.style.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CoupledDecimalField(
    labelA: String,
    itemA: String,
    labelB: String,
    itemB: String,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Column {
        SingleDecimalTextField(
            label = labelA,
            value = itemA,
            interactionSource = interactionSource,
            isHovered = isHovered
        )
        SingleDecimalTextField(
            label = labelB,
            value = itemB,
            interactionSource = interactionSource,
            isHovered = isHovered
        )
    }
}

@Composable
private fun SingleDecimalTextField(
    label: String,
    value: String,
    interactionSource: MutableInteractionSource,
    isHovered: Boolean,
) {
    Text(
        text = label,
        color = lightGrey,
        modifier = Modifier
            .padding(paddingSmall)
            .hoverable(interactionSource = interactionSource)
        ,
        style = MaterialTheme.typography.titleSmall,
    )
    Text(
        text = value,
        color = if (isHovered) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        modifier = Modifier
            .padding(paddingSmall)
        ,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
internal fun CoupledDecimalTextField(
    labelA: String,
    itemA: String,
    labelB: String,
    itemB: String,
    onChangeBothRequest: (String, String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()

    var selectedItemA by remember { mutableStateOf(itemA) }
    var selectedItemB by remember { mutableStateOf(itemB) }

    val decimalCharacters = setOf('+', '-', '.')
    val maxLatitude = 90
    val maxLongitude = 180

    Column {
        SingleDecimalTextField(
            label = labelA,
            value = selectedItemA,
            interactionSource = interactionSource,
            focusRequester = focusRequester,
            isFocused = isFocused,
            isHovered = isHovered
        ) { newValue ->
            selectedItemA = newValue
                .takeWhile { it in decimalCharacters || it.isDigit() }
                .parseDecimals(maxLatitude)
        }
        SingleDecimalTextField(
            label = labelB,
            value = selectedItemB,
            interactionSource = interactionSource,
            focusRequester = focusRequester,
            isFocused = isFocused,
            isHovered = isHovered
        ) { newValue ->
            selectedItemB = newValue
                .takeWhile { it in decimalCharacters || it.isDigit() }
                .parseDecimals(maxLongitude)
        }

        Row(
            modifier = Modifier
                .pointerHoverIcon(PointerIcon.Default)
            ,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TintedIconButton(
                onClick = {
                    selectedItemA = itemA
                    selectedItemB = itemB
                },
                icon = Icons.Sharp.Close,
                smallerIcon = true,
                enabled = (selectedItemA.isNotEmpty() && selectedItemA != itemA ) ||
                        (selectedItemB.isNotEmpty() && selectedItemB != itemB),
                colors = actionButtonColors().copy(disabledContentColor = MaterialTheme.colorScheme.background)
            )
            TintedIconButton(
                onClick = {
                    onChangeBothRequest(selectedItemA, selectedItemB)
                },
                icon = Icons.Sharp.Check,
                smallerIcon = true,
                enabled = selectedItemA.isNotEmpty() && selectedItemB.isNotEmpty() &&
                        (selectedItemA != itemA || selectedItemB != itemB),
                colors = actionButtonColors().copy(disabledContentColor = MaterialTheme.colorScheme.background)
            )
        }
    }
}

@Composable
private fun SingleDecimalTextField(
    label: String,
    value: String,
    interactionSource: MutableInteractionSource,
    focusRequester: FocusRequester,
    isFocused: Boolean,
    isHovered: Boolean,
    onValueChange: (String) -> Unit,
) {
    Text(
        text = label,
        color = lightGrey,
        modifier = Modifier
            .padding(paddingSmall)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { focusRequester.requestFocus() },
            )
        ,
        style = MaterialTheme.typography.titleSmall,
    )
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                if (isFocused) {
                    drawBorder(borderSmall.toPx(), 0f, shadowGreen, StrokeCap.Butt)
                }
            }
            .focusRequester(focusRequester)
        ,
        textStyle = if (isHovered) {
            MaterialTheme.typography.bodyMedium.copy(color = neonBlue)
        } else {
            MaterialTheme.typography.bodyMedium
        },
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        interactionSource = interactionSource
    ) { innerTextField ->
        TextFieldDefaultDecorationBox(
            text = value,
            innerTextField = innerTextField,
            singleLine = true,
            interactionSource = interactionSource,
            placeholder = {
                Text(
                    text = stringResource(Res.string.n_a),
                    style = MaterialTheme.typography.bodyMedium,
                    color = lightGrey
                )
            },
            horizontalPadding = paddingSmall,
            verticalPadding = paddingSmall,
        )
    }
}

private fun String.parseDecimals(max: Int): String {
    val decimalPoint = '.'
    val tokens = split(decimalPoint)
    return if (tokens.size > 1) {
        val first = tokens[0]
        val sign = if (first.startsWith('-')) "-" else ""
        val whole = first.trimStart('-', '+').takeIf { it.isNotEmpty() }
            ?.toInt()?.coerceAtMost(max)
            ?: "0"
        val fractional = tokens.drop(1).joinToString("").take(4)
        "$sign$whole$decimalPoint$fractional"
    } else {
        tokens.joinToString("") + decimalPoint + "0000"
    }
}

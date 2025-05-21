package dev.bogwalk.bootrack.components.utils

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.bogwalk.bootrack.style.lightGrey
import dev.bogwalk.bootrack.style.paddingMedium
import dev.bogwalk.bootrack.style.paddingSmall

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TextFieldDefaultDecorationBox(
    text: String,
    innerTextField: @Composable (() -> Unit),
    singleLine: Boolean,
    interactionSource: MutableInteractionSource,
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    horizontalPadding: Dp = paddingMedium,
    verticalPadding: Dp = paddingSmall / 2,
) {
    val colors = TextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.primary,
        unfocusedTextColor = lightGrey,
        errorTextColor = MaterialTheme.colorScheme.error,
        focusedPlaceholderColor = lightGrey,
        unfocusedPlaceholderColor = lightGrey,
        focusedContainerColor = MaterialTheme.colorScheme.background,
        unfocusedContainerColor = MaterialTheme.colorScheme.background,
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        errorIndicatorColor = MaterialTheme.colorScheme.error,
        errorTrailingIconColor = MaterialTheme.colorScheme.error,
    )
    val shape = RectangleShape

    TextFieldDefaults.DecorationBox(
        value = text,
        innerTextField = innerTextField,
        enabled = true,
        singleLine = singleLine,
        visualTransformation = VisualTransformation.None,
        interactionSource = interactionSource,
        placeholder = placeholder,
        trailingIcon = trailingIcon,
        shape = shape,
        colors = colors,
        contentPadding = PaddingValues(
            horizontal = horizontalPadding,
            vertical = verticalPadding
        ),
        container = {
            TextFieldDefaults.Container(
                enabled = true,
                isError = false,
                interactionSource = interactionSource,
                colors = colors,
                shape = shape,
                unfocusedIndicatorLineThickness = 0.dp,
                focusedIndicatorLineThickness = 0.dp,
            )
        }
    )
}

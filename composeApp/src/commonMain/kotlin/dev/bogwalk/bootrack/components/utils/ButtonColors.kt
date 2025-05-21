package dev.bogwalk.bootrack.components.utils

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.bogwalk.bootrack.style.lightGrey

@Composable
internal fun navigationButtonColors(): IconButtonColors = IconButtonDefaults.iconButtonColors(
    containerColor = MaterialTheme.colorScheme.onSurface, // when hovered
    contentColor = MaterialTheme.colorScheme.primary, // when enabled
    disabledContentColor = lightGrey,
)

@Composable
internal fun actionButtonColors(): IconButtonColors = IconButtonDefaults.iconButtonColors(
    containerColor = MaterialTheme.colorScheme.onSurface, // when hovered
    contentColor = lightGrey, // when enabled
    // should never be disabled
)

@Composable
internal fun errorButtonColors(): IconButtonColors = IconButtonDefaults.iconButtonColors(
    containerColor = MaterialTheme.colorScheme.error, // no difference on hover
    contentColor = MaterialTheme.colorScheme.error, // when enabled
    // should never be disabled
)

@Composable
internal fun displayButtonColors(): IconButtonColors = IconButtonDefaults.iconButtonColors(
    containerColor = MaterialTheme.colorScheme.tertiary, // no difference on hover
    contentColor = MaterialTheme.colorScheme.tertiary, // when enabled
    disabledContentColor = lightGrey,
)

@Composable
internal fun textButtonColors(defaultColor: Color = lightGrey): ButtonColors = ButtonDefaults.textButtonColors(
    containerColor = MaterialTheme.colorScheme.tertiary, // when hovered
    contentColor = defaultColor, // when enabled
    // should never be disabled
)

@Composable
internal fun toggleButtonColors(onColor: Color): IconToggleButtonColors = IconButtonDefaults.iconToggleButtonColors(
    containerColor = lightGrey, // when hovered & off
    contentColor = lightGrey, // when toggled off
    checkedContainerColor = onColor, // when hovered & on
    checkedContentColor = onColor, // when toggled on
    // should never be disabled
)

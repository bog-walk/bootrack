package dev.bogwalk.bootrack.components.utils

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.ui.Alignment

internal fun visibilityEntry(): EnterTransition {
    return fadeIn() + expandIn(spring(stiffness = 200f), expandFrom = Alignment.CenterEnd)
}

internal fun visibilityExit(): ExitTransition {
    return shrinkOut(shrinkTowards = Alignment.CenterEnd) + fadeOut()
}

package dev.bogwalk.bootrack.components.issue

import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import dev.bogwalk.bootrack.style.borderMedium
import dev.bogwalk.bootrack.style.mediumGreen

@Composable
internal actual fun LazyListScrollbar(
    state: LazyListState,
    modifier: Modifier,
) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(state),
        modifier = modifier
        ,
        style = ScrollbarStyle(
            minimalHeight = borderMedium,
            thickness = borderMedium,
            shape = RectangleShape,
            hoverDurationMillis = 500,
            unhoverColor = mediumGreen.copy(alpha = 0.42f),
            hoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.52f)
        )
    )
}

package dev.bogwalk.bootrack.components.issue

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal expect fun LazyListScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier,
)

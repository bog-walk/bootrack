package dev.bogwalk.bootrack.components.topbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import bootrack.composeapp.generated.resources.Res
import bootrack.composeapp.generated.resources.search
import dev.bogwalk.bootrack.components.utils.visibilityEntry
import dev.bogwalk.bootrack.components.utils.visibilityExit
import dev.bogwalk.bootrack.style.searchBarMinWidth
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ExpandableSearch(
    project: String,
    issueCount: Int,
    isFiltered: Boolean,
    issueCode: String?,
    user: String?,
    modifier: Modifier = Modifier,
    onSearchRequest: (String) -> Unit,
    onSearchFilteredRequest: (String) -> Unit,
    onSelectAllIssues: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var filterResolved by remember { mutableStateOf(false) }

    val transition = updateTransition(expanded, label = "search field state")
    val searchBarWeight by transition.animateFloat(
        transitionSpec = { tween() },
        label = "search field weight"
    ) { expanded ->
        if (expanded) 1f else 0.4f
    }

    Row(
        modifier = modifier
        ,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        transition.AnimatedVisibility(
            visible = { !it },
            enter = visibilityEntry(),
            exit = visibilityExit()
        ) {
            NavTree(
                project = project,
                user = user,
                issueCount = issueCount,
                isFiltered = isFiltered,
                issueCode = issueCode,
                onSelectAllIssues = onSelectAllIssues
            )
        }
        MultiOptionSearchBar(
            transition = transition,
            placeHolderText = stringResource(Res.string.search),
            filterResults = filterResolved,
            modifier = Modifier
                .widthIn(min = searchBarMinWidth)
                .fillMaxWidth(searchBarWeight)
                .onFocusChanged { expanded = it.isFocused || it.hasFocus }
            ,
            onFilterRequest = {
                expanded = true
                filterResolved = it
            },
            onSearchRequest = if (filterResolved) onSearchFilteredRequest else onSearchRequest,
            onCloseSearchRequest = { expanded = false },
        )
    }
}

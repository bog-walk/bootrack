package dev.bogwalk.bootrack.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowBackIosNew
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.dp
import bootrack.composeapp.generated.resources.Res
import bootrack.composeapp.generated.resources.new_issue
import dev.bogwalk.bootrack.client.state.MainScreenState
import dev.bogwalk.bootrack.components.buttons.FilledActionButton
import dev.bogwalk.bootrack.components.buttons.TintedIconButton
import dev.bogwalk.bootrack.components.topbar.ExpandableSearch
import dev.bogwalk.bootrack.components.topbar.NavIndex
import dev.bogwalk.bootrack.components.utils.drawBorderWithShadow
import dev.bogwalk.bootrack.components.utils.navigationButtonColors
import dev.bogwalk.bootrack.style.borderSmall
import dev.bogwalk.bootrack.style.paddingSmall
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DynamicTopBar(
    state: MainScreenState,
    project: String?,
    user: String?,
    issueCount: Int,
    issueCode: String?,
    issueIndex: Int?,
    onBackRequest: () -> Unit,
    onPreviousIssueRequest: () -> Unit,
    onNextIssueRequest: () -> Unit,
    onAddNewIssueRequest: () -> Unit,
    onQueryIssuesRequest: (String) -> Unit,
    onFilterIssuesRequest: (String) -> Unit,
    onShowAllIssues: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = paddingSmall / 2, top = paddingSmall, end = paddingSmall)
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                drawBorderWithShadow()
            }
        ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        TintedIconButton(
            onClick = onBackRequest,
            icon = Icons.Sharp.ArrowBackIosNew,
            smallerIcon = true,
            enabled = state != MainScreenState.ISSUES_LIST,
            colors = navigationButtonColors()
        )

        project?.let {
            ExpandableSearch(
                project = if (state != MainScreenState.USER_ACCOUNT) it else "",
                issueCount = issueCount,
                isFiltered = state == MainScreenState.FILTERED_ISSUES_LIST,
                issueCode = issueCode,
                user = if (state != MainScreenState.USER_ACCOUNT) null else user,
                modifier = Modifier
                    .padding(end = if (issueIndex == null) paddingSmall else 0.dp)
                    .weight(1f)
                ,
                onSearchRequest = onQueryIssuesRequest,
                onSearchFilteredRequest = onFilterIssuesRequest,
                onSelectAllIssues = onShowAllIssues
            )
        } ?: Spacer(Modifier.weight(1f))

        issueIndex?.let {
            NavIndex(
                index = it,
                count = issueCount,
                onPreviousRequest = onPreviousIssueRequest,
                onNextRequest = onNextIssueRequest
            )
        }

        FilledActionButton(
            onClick = onAddNewIssueRequest,
            text = stringResource(Res.string.new_issue),
            modifier = Modifier
                .padding(top = paddingSmall + borderSmall, end = paddingSmall + borderSmall, bottom = paddingSmall + borderSmall)
            ,
            enabled = project != null,
        )
    }
}

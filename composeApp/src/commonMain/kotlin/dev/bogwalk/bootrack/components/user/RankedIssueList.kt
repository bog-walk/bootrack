package dev.bogwalk.bootrack.components.user

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Leaderboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import bootrack.composeapp.generated.resources.Res
import bootrack.composeapp.generated.resources.rank_tt
import bootrack.composeapp.generated.resources.refresh_results
import dev.bogwalk.bootrack.components.buttons.FilledActionButton
import dev.bogwalk.bootrack.components.buttons.TintedIconButton
import dev.bogwalk.bootrack.components.issue.SummaryCard
import dev.bogwalk.bootrack.components.utils.UserRankBy
import dev.bogwalk.bootrack.components.utils.getLabel
import dev.bogwalk.bootrack.model.Issue
import dev.bogwalk.bootrack.model.IssueSummarized
import dev.bogwalk.bootrack.model.User
import dev.bogwalk.bootrack.style.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RankedIssueList(
    user: User,
    users: List<User>,
    issues: List<List<IssueSummarized>>,
    currentRankBy: UserRankBy,
    onRefreshSearch: (UserRankBy) -> Unit,
    onShowIssueRequest: (IssueSummarized) -> Unit,
    onWatchIssueRequest: (Issue, String, Boolean) -> Unit,
    onUpvoteIssueRequest: (Issue, String, Boolean) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedRankOption by remember { mutableStateOf(currentRankBy) }

    Column(
        modifier = Modifier
            .padding(start = paddingLarge, top = paddingMedium, end = paddingLarge, bottom = paddingLarge)
            .verticalScroll(rememberScrollState())
        ,
    ) {
        issues.forEachIndexed { index,  list ->
            list.forEach { issue ->
                SummaryCard(
                    issue = issue,
                    user = users.first { it.id == user.id },
                    author = users.first { it.id == issue.issue.authorId },
                    assignee = issue.issue.assigneeId?.let { id -> users.firstOrNull { it.id == id } },
                    onShowIssueRequest = onShowIssueRequest,
                    onWatchIssueRequest = onWatchIssueRequest,
                    onUpvoteIssueRequest = onUpvoteIssueRequest,
                )
            }

            if (index < 2) {
                HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = paddingSmall, vertical = paddingMedium)
                    ,
                    thickness = borderSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Row(
            modifier = Modifier
                .padding(top = paddingMedium)
            ,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledActionButton(
                onClick = {
                    onRefreshSearch(selectedRankOption)
                },
                text = stringResource(Res.string.refresh_results),
            )

            Box(
                modifier = Modifier
                    .padding(start = paddingMedium)
                ,
            ) {
                TintedIconButton(
                    onClick = { expanded = true },
                    icon = Icons.Sharp.Leaderboard,
                    tooltipText = stringResource(Res.string.rank_tt),
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    shape = MaterialTheme.shapes.small,
                    containerColor = MaterialTheme.colorScheme.background,
                    shadowElevation = paddingMedium,
                ) {
                    UserRankBy.entries.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.getLabel(),
                                    color = if (selectedRankOption == option) MaterialTheme.colorScheme.tertiary else lightGrey,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            onClick = {
                                if (selectedRankOption != option) {
                                    selectedRankOption = option
                                }
                                expanded = false
                            },
                        )
                    }
                }
            }
        }
    }
}

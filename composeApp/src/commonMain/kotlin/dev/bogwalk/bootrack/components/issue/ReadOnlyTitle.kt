package dev.bogwalk.bootrack.components.issue

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Edit
import androidx.compose.material.icons.sharp.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import bootrack.composeapp.generated.resources.Res
import bootrack.composeapp.generated.resources.issue_delete
import bootrack.composeapp.generated.resources.issue_edit_tt
import bootrack.composeapp.generated.resources.issue_watch_tt
import dev.bogwalk.bootrack.components.buttons.TintedIconButton
import dev.bogwalk.bootrack.components.buttons.TintedToggleButton
import dev.bogwalk.bootrack.model.Issue
import dev.bogwalk.bootrack.model.IssueDetailed
import dev.bogwalk.bootrack.model.IssueState
import dev.bogwalk.bootrack.style.ghostWhite
import dev.bogwalk.bootrack.style.lightGrey
import dev.bogwalk.bootrack.style.paddingSmall
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ReadOnlyTitle(
    issue: IssueDetailed,
    userId: Int,
    modifier: Modifier = Modifier,
    onWatchIssueRequest: (Issue, String, Boolean) -> Unit,
    onEditIssueRequest: (IssueDetailed) -> Unit,
    onDeleteIssueRequest: (IssueDetailed) -> Unit,
) {
    Row(
        modifier = modifier
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TintedToggleButton(
            onToggle = { selected -> onWatchIssueRequest(issue.issue, issue.issue.code, selected) },
            checked = userId in issue.issue.watchers,
            icon = Icons.Sharp.Star,
            tooltipText = stringResource(Res.string.issue_watch_tt),
        )
        Text(
            text = issue.issue.title,
            modifier = Modifier
                .weight(1f)
                .padding(start = paddingSmall)
            ,
            textDecoration = if (issue.issue.state == IssueState.COMPLETED) {
                TextDecoration.LineThrough
            } else {
                TextDecoration.None
            },
            color = if (issue.issue.state == IssueState.COMPLETED) lightGrey else ghostWhite,
            style = MaterialTheme.typography.bodyMedium,
        )
        TintedIconButton(
            onClick = { onEditIssueRequest(issue) },
            smallerIcon = true,
            modifier = Modifier
                .padding(start = paddingSmall)
            ,
            icon = Icons.Sharp.Edit,
            tooltipText = stringResource(Res.string.issue_edit_tt)
        )
        IconDropdownMenu(
            deleteText = stringResource(Res.string.issue_delete),
            onSelectDelete = { onDeleteIssueRequest(issue) }
        )
    }
}

package dev.bogwalk.bootrack.components.issue

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ChatBubble
import androidx.compose.material.icons.sharp.Star
import androidx.compose.material.icons.sharp.ThumbUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import bootrack.composeapp.generated.resources.Res
import bootrack.composeapp.generated.resources.issue_comment_tt
import bootrack.composeapp.generated.resources.issue_upvote_tt
import bootrack.composeapp.generated.resources.issue_watch_tt
import dev.bogwalk.bootrack.components.buttons.NumberedIconButton
import dev.bogwalk.bootrack.components.buttons.NumberedToggleButton
import dev.bogwalk.bootrack.components.buttons.TintedToggleButton
import dev.bogwalk.bootrack.model.Issue
import dev.bogwalk.bootrack.model.IssueState
import dev.bogwalk.bootrack.model.IssueSummarized
import dev.bogwalk.bootrack.style.ghostWhite
import dev.bogwalk.bootrack.style.lightGrey
import dev.bogwalk.bootrack.style.paddingMedium
import dev.bogwalk.bootrack.style.paddingSmall
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MainDetailsRow(
    issue: IssueSummarized,
    userId: Int,
    onSeeCommentsRequest: (Issue) -> Unit,
    onWatchIssueRequest: (IssueSummarized, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onUpvoteIssueRequest: ((IssueSummarized, Boolean) -> Unit)? = null,
) {
    Row(
        modifier = modifier
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TintedToggleButton(
            onToggle = { selected -> onWatchIssueRequest(issue, selected) },
            checked = userId in issue.issue.watchers,
            icon = Icons.Sharp.Star,
            smallerIcon = true,
            modifier = Modifier
                .padding(start = paddingSmall / 2)
            ,
            tooltipText = stringResource(Res.string.issue_watch_tt),
        )
        PrioritySymbol(
            priority = issue.issue.priority,
            modifier = Modifier
                .padding(end = paddingSmall)
        )
        Text(
            text = issue.issue.code,
            modifier = Modifier
                .padding(end = paddingMedium)
            ,
            color = if (issue.issue.state == IssueState.COMPLETED) lightGrey else ghostWhite,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = issue.issue.title,
            modifier = Modifier
                .weight(1f)
            ,
            textDecoration = if (issue.issue.state == IssueState.COMPLETED) {
                TextDecoration.LineThrough
            } else {
                TextDecoration.None
            },
            overflow = TextOverflow.Ellipsis,
            color = if (issue.issue.state == IssueState.COMPLETED) lightGrey else ghostWhite,
            maxLines = 1,
            style = MaterialTheme.typography.bodyMedium,
        )
        onUpvoteIssueRequest?.let {
            NumberedToggleButton(
                onToggle = { selected -> it(issue, selected) },
                checked = userId in issue.issue.upvotes,
                count = issue.issue.upvotes.size,
                icon = Icons.Sharp.ThumbUp,
                modifier = Modifier.offset(x = paddingSmall),
                tooltipText = stringResource(Res.string.issue_upvote_tt)
            )
            NumberedIconButton(
                onClick = { onSeeCommentsRequest(issue.issue) },
                count = issue.commentCount,
                icon = Icons.Sharp.ChatBubble,
                tooltipText = stringResource(Res.string.issue_comment_tt)
            )
        }
    }
}

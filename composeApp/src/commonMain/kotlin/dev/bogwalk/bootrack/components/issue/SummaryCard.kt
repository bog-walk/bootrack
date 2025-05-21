package dev.bogwalk.bootrack.components.issue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.semantics.Role
import dev.bogwalk.bootrack.components.utils.drawSummaryCardBorder
import dev.bogwalk.bootrack.model.Issue
import dev.bogwalk.bootrack.model.IssueSummarized
import dev.bogwalk.bootrack.model.User
import dev.bogwalk.bootrack.model.format
import dev.bogwalk.bootrack.style.paddingMedium
import dev.bogwalk.bootrack.style.paddingSmall

@Composable
internal fun SummaryCard(
    issue: IssueSummarized,
    user: User,
    author: User,
    assignee: User?,
    onShowIssueRequest: (IssueSummarized) -> Unit,
    onWatchIssueRequest: (Issue, String, Boolean) -> Unit,
    onUpvoteIssueRequest: (Issue, String, Boolean) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Column(
        modifier = Modifier
            .padding(bottom = paddingMedium)
            .background(
                if (isHovered) {
                    MaterialTheme.colorScheme.background.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.background
                }
            )
            .drawBehind {
                drawSummaryCardBorder(isHovered)
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button
            ) {
                onShowIssueRequest(issue)
            },
    ) {
        MainDetailsRow(
            issue = issue,
            userId = user.id,
            onSeeCommentsRequest = { onShowIssueRequest(issue) },
            onWatchIssueRequest = { issue, toggle ->
                onWatchIssueRequest(issue.issue, issue.issue.code, toggle)
                                  },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = paddingSmall, end = paddingMedium, bottom  = paddingSmall / 2)
            ,
            onUpvoteIssueRequest = { issue, toggle ->
                onUpvoteIssueRequest(issue.issue, issue.issue.code, toggle)
            }
        )
        ExtraDetailsRow(
            assignee = assignee,
            state = issue.issue.state,
            author = author,
            modified = issue.issue.modifiedAt.format(user.settings.dateFormat),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = paddingSmall, top = paddingSmall / 2, end = paddingMedium, bottom  = paddingMedium)
        )
    }
}

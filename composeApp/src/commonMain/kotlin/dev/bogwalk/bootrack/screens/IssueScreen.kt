package dev.bogwalk.bootrack.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Star
import androidx.compose.material.icons.sharp.ThumbUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import bootrack.composeapp.generated.resources.Res
import bootrack.composeapp.generated.resources.issue_upvote_tt
import dev.bogwalk.bootrack.components.buttons.NumberedIconButton
import dev.bogwalk.bootrack.components.buttons.NumberedToggleButton
import dev.bogwalk.bootrack.components.comment.EditableIconMessage
import dev.bogwalk.bootrack.components.comment.IconTextField
import dev.bogwalk.bootrack.components.issue.EditableDetails
import dev.bogwalk.bootrack.components.issue.FieldBox
import dev.bogwalk.bootrack.components.issue.ReadOnlyTitle
import dev.bogwalk.bootrack.components.issue.TimestampDetailsRow
import dev.bogwalk.bootrack.components.utils.DummyValues
import dev.bogwalk.bootrack.components.utils.avatar
import dev.bogwalk.bootrack.model.*
import dev.bogwalk.bootrack.style.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun IssueScreen(
    projects: List<Project>,
    users: List<User>,
    issue: IssueDetailed,
    user: User,
    onWatchIssueRequest: (Issue, String, Boolean) -> Unit,
    onUpvoteIssueRequest: (Issue, String, Boolean) -> Unit,
    onEditIssueRequest: (IssueDetailed) -> Unit,
    onDeleteIssueRequest: (IssueDetailed) -> Unit,
    onAddCommentRequest: (Comment) -> Unit,
    onEditCommentRequest: (Comment) -> Unit,
    onDeleteCommentRequest: (Comment) -> Unit,
) {
    var inEditMode by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(paddingSmall)
        ,
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .weight(1f)
                .padding(start = paddingSmall, bottom = paddingLarge)
            ,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // sticky header
            Column(
              modifier = Modifier.fillMaxWidth()
            ) {
                TimestampDetailsRow(
                    author = users.first { it.id == issue.issue.authorId },
                    created = issue.issue.createdAt.format(user.settings.dateFormat),
                    modified = issue.issue.modifiedAt.format(user.settings.dateFormat),
                    timestampsDiffer = issue.issue.createdAt != issue.issue.modifiedAt,
                    modifier = Modifier
                        .padding(start = paddingSmall, top = paddingMedium, end = paddingMedium, bottom  = paddingSmall / 2)
                )
                if (!inEditMode) {
                    ReadOnlyTitle(
                        issue = issue,
                        userId = user.id,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = paddingSmall, top = paddingSmall / 2, end = paddingMedium, bottom  = paddingMedium)
                        ,
                        onWatchIssueRequest = onWatchIssueRequest,
                        onEditIssueRequest = { inEditMode = true },
                        onDeleteIssueRequest = onDeleteIssueRequest
                    )
                }
            }

            // main body: edit + read-only + comments
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                ,
            ) {
                if (inEditMode) {
                    EditableDetails(
                        issue = issue,
                        userId = user.id,
                        onWatchIssueRequest = { s, b -> onWatchIssueRequest(s.issue, s.issue.code, b) },
                        onEditIssueRequest = {
                            inEditMode = false
                            if (user.settings.starOnIssueUpdate && user.id !in it.issue.watchers) {
                                onEditIssueRequest(it.copy(issue = it.issue.copy(watchers = it.issue.watchers + user.id)))
                            } else {
                                onEditIssueRequest(it)
                            }
                        }
                    ) {
                        inEditMode = false
                    }
                } else {
                    Text(
                        text = issue.issue.description,
                        modifier = Modifier
                            .padding(paddingMedium)
                            .fillMaxWidth()
                        ,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = paddingSmall, vertical = paddingMedium)
                    ,
                    thickness = borderSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                issue.comments.sortedBy { it.createdAt }.forEach { comment ->
                    EditableIconMessage(
                        comment = comment,
                        author = users.first { it.id == comment.authorId },
                        user = user,
                        users = users,
                        onEditCommentRequest = onEditCommentRequest,
                        onDeleteCommentRequest = onDeleteCommentRequest,
                    )
                }

                IconTextField(
                    icon = user.settings.avatar().icon,
                    tint = user.settings.avatar().tint,
                    users = users,
                    modifier = Modifier
                        .padding(top = paddingLarge)
                    ,
                    onAddCommentRequest = { content ->
                        val newComment = Comment(
                            id = DummyValues.IDL,
                            authorId = user.id,
                            content = content,
                            issueNumber = issue.issue.number,
                            projectId = issue.issue.projectId,
                            createdAt = DummyValues.TS,
                            modifiedAt = DummyValues.TS
                        )
                        onAddCommentRequest(newComment)
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .widthIn(min = fieldBoxCompactWidth, max = fieldBoxMaxWidth)
                .padding(horizontal = paddingSmall)
            ,
        ) {
            Row {
                NumberedToggleButton(
                    onToggle = { selected -> onUpvoteIssueRequest(issue.issue, issue.issue.code, selected) },
                    checked = user.id in issue.issue.upvotes,
                    count = issue.issue.upvotes.size,
                    icon = Icons.Sharp.ThumbUp,
                    tooltipText = stringResource(Res.string.issue_upvote_tt)
                )
                NumberedIconButton(
                    onClick = {},
                    count = issue.issue.watchers.size,
                    icon = Icons.Sharp.Star,
                    modifier = Modifier.offset(x = -paddingSmall),
                    enabled = false,
                )
            }
            FieldBox(
                projects = projects,
                users = users,
                issue = issue,
            ) {
                when (it) {
                    is IssuePriority -> onEditIssueRequest(issue.copy(issue = issue.issue.copy(priority = it)))
                    is IssueState -> onEditIssueRequest(issue.copy(issue = issue.issue.copy(state = it)))
                    is String -> {
                        val newAssignee = users.find { u -> u.fullName == it }
                        val updatedWatchers = issue.issue.watchers
                            .toMutableSet()
                            .plus(newAssignee?.takeIf { u -> u.settings.starOnIssueAssigned }?.id)
                            .filterNotNull()
                        onEditIssueRequest(issue.copy(issue = issue.issue.copy(assigneeId = newAssignee?.id, watchers = updatedWatchers)))
                    }
                    else -> error("Invalid input found for IssueScreen: $it of type ${it::class.simpleName}")
                }
            }
        }
    }
}

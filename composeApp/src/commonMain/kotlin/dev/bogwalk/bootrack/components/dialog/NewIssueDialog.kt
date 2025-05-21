package dev.bogwalk.bootrack.components.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import bootrack.composeapp.generated.resources.*
import dev.bogwalk.bootrack.components.buttons.FilledActionButton
import dev.bogwalk.bootrack.components.buttons.OutlinedActionButton
import dev.bogwalk.bootrack.components.issue.FieldBox
import dev.bogwalk.bootrack.components.utils.DummyValues
import dev.bogwalk.bootrack.components.utils.TextFieldDefaultDecorationBox
import dev.bogwalk.bootrack.components.utils.drawBorder
import dev.bogwalk.bootrack.components.utils.drawDialogBorder
import dev.bogwalk.bootrack.model.*
import dev.bogwalk.bootrack.style.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun NewIssueDialog(
    projects: List<Project>,
    currentProject: Project,
    users: List<User>,
    currentUser: User,
    onCancelNewIssue: () -> Unit,
    onAddNewIssueRequest: (Issue) -> Unit
) {
    var titleText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }

    val titleInteractionSource = remember { MutableInteractionSource() }
    val descriptionInteractionSource = remember { MutableInteractionSource() }

    var project by remember { mutableStateOf(currentProject) }
    var priority by remember { mutableStateOf(IssuePriority.NORMAL) }
    var assignee by remember { mutableStateOf<User?>(null) }
    var state by remember { mutableStateOf(IssueState.SUBMITTED) }
    var location by remember { mutableStateOf<Location?>(null) }


    Row(
        modifier = Modifier
            .padding(paddingSmall)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                drawDialogBorder()
            }
            .padding(paddingSmall)
        ,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(paddingSmall)
                .weight(0.7f)
        ) {
            BasicTextField(
                value = titleText,
                onValueChange = { titleText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = paddingSmall, start = paddingSmall, end = paddingSmall, bottom = paddingMedium)
                    .drawBehind {
                        drawBorder(borderSmall.toPx(), 0f, shadowGreen, StrokeCap.Butt)
                    }
                ,
                textStyle = MaterialTheme.typography.bodyMedium,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                interactionSource = titleInteractionSource
            ) { innerTextField ->
                TextFieldDefaultDecorationBox(
                    text = titleText,
                    innerTextField = innerTextField,
                    singleLine = false,
                    interactionSource = titleInteractionSource,
                    placeholder = {
                        Text(
                            stringResource(Res.string.summary),
                            style = MaterialTheme.typography.bodyMedium,
                            color = lightGrey
                        )
                    }
                )
            }
            BasicTextField(
                value = descriptionText,
                onValueChange = { descriptionText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingSmall)
                    .weight(1f)
                    .drawBehind {
                        drawBorder(2.dp.toPx(), 0f, shadowGreen, StrokeCap.Butt)
                    }
                ,
                textStyle = MaterialTheme.typography.bodyMedium,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                interactionSource = descriptionInteractionSource
            ) { innerTextField ->
                TextFieldDefaultDecorationBox(
                    text = descriptionText,
                    innerTextField = innerTextField,
                    singleLine = false,
                    interactionSource = descriptionInteractionSource,
                    placeholder = {
                        Text(
                            text = stringResource(Res.string.description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = lightGrey
                        )
                    },
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = paddingSmall)
                ,
                horizontalArrangement = Arrangement.End
            ) {
                FilledActionButton(
                    onClick = {
                        val newWatchers = listOfNotNull(
                            currentUser.takeIf { it.settings.starOnIssueCreate }?.id,
                            assignee?.takeIf { it.settings.starOnIssueAssigned }?.id
                        )
                        val newIssue = Issue(
                            number = DummyValues.IDL,
                            projectId = project.id,
                            code = currentProject.code,
                            authorId = currentUser.id,
                            title = titleText,
                            description = descriptionText,
                            priority = priority,
                            state = state,
                            assigneeId = assignee?.id,
                            location = location,
                            watchers = newWatchers,
                            upvotes = emptyList(),
                            createdAt = DummyValues.TS,
                            modifiedAt = DummyValues.TS,
                        )
                        onAddNewIssueRequest(newIssue)
                    },
                    text = stringResource(Res.string.save),
                    enabled = titleText.isNotEmpty(),
                )
                OutlinedActionButton(
                    onClick = onCancelNewIssue,
                    text = stringResource(Res.string.cancel)
                )
            }
        }

        FieldBox(
            projects = projects,
            users = users,
            issue = null,
            selectedProject = project,
            selectedPriority = priority,
            selectedUser = assignee,
            selectedState = state,
            selectedLocation = location,
            modifier = Modifier
                .weight(0.25f)
            ,
        ) {
            when (it) {
                is Project -> project = it
                is IssuePriority -> priority = it
                is IssueState -> state = it
                is Location -> location = it
                is String -> assignee = users.find { u -> u.fullName == it }
                else -> error("Invalid input found for NewIssueDialog: $it of type ${it::class.simpleName}")
            }
        }
    }
}
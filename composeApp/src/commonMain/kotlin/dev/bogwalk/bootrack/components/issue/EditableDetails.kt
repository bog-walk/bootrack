package dev.bogwalk.bootrack.components.issue

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import bootrack.composeapp.generated.resources.*
import dev.bogwalk.bootrack.components.buttons.FilledActionButton
import dev.bogwalk.bootrack.components.buttons.OutlinedActionButton
import dev.bogwalk.bootrack.components.buttons.TintedToggleButton
import dev.bogwalk.bootrack.components.utils.TextFieldDefaultDecorationBox
import dev.bogwalk.bootrack.components.utils.drawBorder
import dev.bogwalk.bootrack.model.IssueDetailed
import dev.bogwalk.bootrack.style.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditableDetails(
    issue: IssueDetailed,
    userId: Int,
    modifier: Modifier = Modifier,
    onWatchIssueRequest: (IssueDetailed, Boolean) -> Unit,
    onEditIssueRequest: (IssueDetailed) -> Unit,
    onCancelRequest: () -> Unit,
) {
    var titleText by remember { mutableStateOf(issue.issue.title) }
    var descriptionText by remember { mutableStateOf(issue.issue.description) }

    val titleInteractionSource = remember { MutableInteractionSource() }
    val descriptionInteractionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .padding(bottom = paddingSmall)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TintedToggleButton(
            onToggle = { selected ->
                onWatchIssueRequest(issue, selected)
            },
            checked = userId in issue.issue.watchers,
            icon = Icons.Sharp.Star,
            tooltipText = stringResource(Res.string.issue_watch_tt),
        )
        BasicTextField(
            value = titleText,
            onValueChange = { titleText = it },
            modifier = modifier
                .fillMaxWidth()
                .padding(top = paddingSmall, end = paddingMedium, bottom = paddingSmall)
                .drawBehind {
                    drawLine(shadowGreen, Offset(0f, size.height), Offset(size.width, size.height), borderSmall.toPx(), StrokeCap.Butt)
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
            )
        }
    }

    BasicTextField(
        value = descriptionText,
        onValueChange = { descriptionText = it },
        modifier = modifier
            .fillMaxWidth()
            .padding(start = paddingMedium, end = paddingMedium, bottom = paddingMedium)
            .drawBehind {
                drawBorder(borderSmall.toPx(), 0f, shadowGreen, StrokeCap.Butt)
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
            .padding(end = paddingSmall, bottom = paddingSmall)
        ,
        horizontalArrangement = Arrangement.End
    ) {
        FilledActionButton(
            onClick = {
                onEditIssueRequest(issue.copy(issue = issue.issue.copy(title = titleText, description = descriptionText)))
            },
            text = stringResource(Res.string.save),
            enabled = issue.issue.title != titleText || issue.issue.description != descriptionText,
        )
        OutlinedActionButton(
            onClick = onCancelRequest,
            modifier = Modifier,
            text = stringResource(Res.string.cancel)
        )
    }
}

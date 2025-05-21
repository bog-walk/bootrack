package dev.bogwalk.bootrack.components.issue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import bootrack.composeapp.generated.resources.Res
import bootrack.composeapp.generated.resources.unassigned
import dev.bogwalk.bootrack.components.buttons.InteractionTextPopup
import dev.bogwalk.bootrack.components.user.UserProfileSummary
import dev.bogwalk.bootrack.model.IssueState
import dev.bogwalk.bootrack.model.User
import dev.bogwalk.bootrack.style.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ExtraDetailsRow(
    assignee: User?,
    state: IssueState,
    author: User,
    modified: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.requiredWidth(paddingSmall / 2 + iconContainerSizeSmall))
        assignee?.let {
            InteractionTextPopup(
                text = it.fullName,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .weight(0.4f)
                ,
            ) {
                UserProfileSummary(
                    user = it,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .shadow(paddingMedium)
                    ,
                )
            }
        } ?: Text(
            text = stringResource(Res.string.unassigned),
            modifier = Modifier
                .weight(0.4f)
            ,
            style = MaterialTheme.typography.titleSmall
        )

        Text(
            text = state.label,
            modifier = Modifier
                .weight(0.2f)
            ,
            color = when (state) {
                IssueState.COMPLETED -> MaterialTheme.colorScheme.primary
                IssueState.IN_PROGRESS -> MaterialTheme.colorScheme.tertiary
                IssueState.SUBMITTED -> lightGrey
            },
            style = MaterialTheme.typography.titleSmall
        )
        InteractionTextPopup(
            text = author.fullName,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .weight(0.4f)
            ,
        ) {
            UserProfileSummary(
                user = author,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .shadow(paddingMedium)
                ,
            )
        }
        Text(
            text = modified,
            modifier = Modifier
                .requiredWidthIn(min = iconContainerSize * 3)
                .padding(end = paddingLarge / 3)
            ,
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.titleSmall
        )
    }
}

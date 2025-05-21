package dev.bogwalk.bootrack.components.comment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import bootrack.composeapp.generated.resources.Res
import bootrack.composeapp.generated.resources.comment_delete
import bootrack.composeapp.generated.resources.comment_edit_tt
import bootrack.composeapp.generated.resources.edited
import dev.bogwalk.bootrack.components.buttons.TintedIconButton
import dev.bogwalk.bootrack.components.issue.IconDropdownMenu
import dev.bogwalk.bootrack.components.utils.annotatedStringWithMention
import dev.bogwalk.bootrack.components.utils.avatar
import dev.bogwalk.bootrack.model.Comment
import dev.bogwalk.bootrack.model.User
import dev.bogwalk.bootrack.style.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditableIconMessage(
    comment: Comment,
    author: User,
    user: User,
    users: List<User>,
    onEditCommentRequest: (Comment) -> Unit,
    onDeleteCommentRequest: (Comment) -> Unit,
) {
    var inEditMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(bottom = paddingSmall)
            .fillMaxWidth()
        ,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.requiredWidth(paddingLarge + iconContainerSize))
            Text(
                text = author.fullName,
                modifier = Modifier
                    .requiredHeight(iconContainerSizeSmall)
                    .wrapContentHeight()
                ,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (!inEditMode && author.id == user.id) {
                Spacer(Modifier.weight(1f))
                TintedIconButton(
                    onClick = { inEditMode = true },
                    smallerIcon = true,
                    icon = Icons.Sharp.Edit,
                    tooltipText = stringResource(Res.string.comment_edit_tt)
                )
                IconDropdownMenu(
                    deleteText = stringResource(Res.string.comment_delete),
                    onSelectDelete = { onDeleteCommentRequest(comment) }
                )
            }
        }

        if (!inEditMode) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = author.settings.avatar().icon,
                    contentDescription = "",
                    modifier = Modifier
                        .padding(horizontal = paddingMedium)
                        .requiredSize(iconContainerSize)
                        .clip(CircleShape)
                    ,
                    tint = author.settings.avatar().tint
                )
                Text(
                    text = annotatedStringWithMention(comment.content),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            IconTextField(
                icon = user.settings.avatar().icon,
                tint = user.settings.avatar().tint,
                users = users,
                content = comment.content,
                onEditCommentRequest = {
                    onEditCommentRequest(comment.copy(content = it))
                    inEditMode = false
                },
                onCancelRequest = { inEditMode = false }
            )
        }

        if (!inEditMode && comment.modifiedAt != comment.createdAt) {
            Row {
                Spacer(Modifier.requiredWidth(paddingLarge + iconContainerSize))
                Text(
                    text = stringResource(Res.string.edited),
                    modifier = Modifier
                        .padding(top = paddingSmall)
                    ,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
    }
}

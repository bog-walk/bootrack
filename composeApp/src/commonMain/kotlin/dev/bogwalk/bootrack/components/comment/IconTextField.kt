package dev.bogwalk.bootrack.components.comment

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import bootrack.composeapp.generated.resources.*
import dev.bogwalk.bootrack.components.buttons.FilledActionButton
import dev.bogwalk.bootrack.components.buttons.OutlinedActionButton
import dev.bogwalk.bootrack.components.user.UserProfileSummary
import dev.bogwalk.bootrack.components.utils.TextFieldDefaultDecorationBox
import dev.bogwalk.bootrack.components.utils.annotatedStringWithMention
import dev.bogwalk.bootrack.components.utils.drawSearchBorder
import dev.bogwalk.bootrack.model.User
import dev.bogwalk.bootrack.style.iconContainerSize
import dev.bogwalk.bootrack.style.lightGrey
import dev.bogwalk.bootrack.style.paddingMedium
import dev.bogwalk.bootrack.style.paddingSmall
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconTextField(
    icon: ImageVector,
    tint: Color,
    users: List<User>,
    content: String? = null,
    modifier: Modifier = Modifier,
    onAddCommentRequest: ((String) -> Unit)? = null,
    onEditCommentRequest: ((String) -> Unit)? = null,
    onCancelRequest: (() -> Unit)? = null,
) {
    var commentText by remember { mutableStateOf(TextFieldValue(content ?: "")) }
    var showAllUsers by remember { mutableStateOf(false) }
    var filteredUsers by remember { mutableStateOf(users) }

    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
        ,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "",
                modifier = Modifier
                    .padding(horizontal = paddingMedium)
                    .requiredSize(iconContainerSize)
                    .clip(CircleShape)
                ,
                tint = tint
            )
            ExposedDropdownMenuBox(
                expanded = showAllUsers,
                onExpandedChange = {
                    if (commentText.text.isNotEmpty()) {
                        showAllUsers = !showAllUsers
                    }
                },
                modifier = Modifier
                    .padding(paddingSmall)
            ) {
                val annotatedText = annotatedStringWithMention(commentText.text)
                BasicTextField(
                    value = commentText,
                    onValueChange = {
                        val mentionIndex = it.text.lastIndexOf('@')
                        showAllUsers = if (mentionIndex != -1) {
                            val username = it.text.substring(mentionIndex + 1)
                            filteredUsers = users.filter { u -> u.fullName.contains(username, ignoreCase = true) }
                            ' ' !in username
                        } else {
                            false
                        }
                        commentText = it
                    },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                        .fillMaxWidth()
                        .heightIn(min = iconContainerSize)
                        .padding(top = paddingSmall, end = paddingMedium, bottom = paddingSmall)
                        .drawBehind {
                            drawSearchBorder()
                        }
                    ,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    visualTransformation = {
                        TransformedText(
                            text = annotatedText,
                            offsetMapping = object : OffsetMapping {
                                override fun originalToTransformed(offset: Int): Int = offset
                                override fun transformedToOriginal(offset: Int): Int = offset
                            }
                        )
                    },
                    interactionSource = interactionSource,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                ) { innerTextField ->
                    TextFieldDefaultDecorationBox(
                        text = commentText.text,
                        innerTextField = innerTextField,
                        singleLine = false,
                        interactionSource = interactionSource,
                        placeholder = {
                            Text(
                                text = stringResource(Res.string.comment),
                                style = MaterialTheme.typography.bodyMedium,
                                color = lightGrey
                            )
                        },
                    )
                }
                ExposedDropdownMenu(
                    expanded = showAllUsers,
                    onDismissRequest = { showAllUsers = false },
                    modifier = Modifier
                        .heightIn(max = iconContainerSize * 4)
                    ,
                    matchTextFieldWidth = false,
                    shape = MaterialTheme.shapes.small,
                    containerColor = MaterialTheme.colorScheme.background,
                    shadowElevation = paddingMedium,
                ) {
                    filteredUsers.takeIf { it.isNotEmpty() }?.forEach { user ->
                        DropdownMenuItem(
                            text = { UserProfileSummary(user) },
                            onClick =  {
                                val newText = commentText.text.replaceAfterLast('@', "${user.username} ")
                                commentText = commentText.copy(
                                    text = newText,
                                    selection = TextRange(newText.length)
                                )
                                showAllUsers = false
                            },
                            contentPadding = PaddingValues(0.dp)
                        )
                    }
                }
            }

        }

        if (commentText.text.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = paddingSmall, bottom = paddingSmall)
                ,
                horizontalArrangement = Arrangement.End
            ) {
                onAddCommentRequest?.let {
                    FilledActionButton(
                        onClick = {
                            it(commentText.text)
                            commentText = TextFieldValue("")
                        },
                        text = stringResource(Res.string.add),
                        enabled = commentText.text.isNotEmpty()
                    )
                    OutlinedActionButton(
                        onClick = { commentText = TextFieldValue("") },
                        text = stringResource(Res.string.cancel)
                    )
                }
                onEditCommentRequest?.let {
                    FilledActionButton(
                        onClick = {
                            it(commentText.text)
                            commentText = TextFieldValue("")
                        },
                        text = stringResource(Res.string.save),
                        enabled = commentText.text.isNotEmpty() && commentText.text != content
                    )
                }
                onCancelRequest?.let {
                    OutlinedActionButton(
                        onClick = { it() },
                        text = stringResource(Res.string.cancel)
                    )
                }
            }
        }
    }
}

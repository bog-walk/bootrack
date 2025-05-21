package dev.bogwalk.bootrack.components.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import bootrack.composeapp.generated.resources.*
import dev.bogwalk.bootrack.components.buttons.FilledActionButton
import dev.bogwalk.bootrack.model.User
import dev.bogwalk.bootrack.model.UserSettings
import dev.bogwalk.bootrack.style.iconContainerSize
import dev.bogwalk.bootrack.style.paddingLarge
import dev.bogwalk.bootrack.style.paddingMedium
import dev.bogwalk.bootrack.style.searchBarMaxWidth
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun NotificationSettings(
    user: User,
    onEditUserSettings: (UserSettings) -> Unit,
) {
    var settings by remember { mutableStateOf(user.settings) }

    Column(
        modifier = Modifier
            .padding(start = paddingLarge, top = paddingMedium, bottom = paddingLarge)
        ,
        verticalArrangement = Arrangement.spacedBy(paddingLarge)
    ) {
        Row {
            SettingsGroupText(
                text = stringResource(Res.string.notification_events)
            )
            Column {
                CheckboxSetting(
                    label = stringResource(Res.string.notify_changes),
                    isChecked = user.settings.notifyOnSelfChanges
                ) {
                    settings = settings.copy(notifyOnSelfChanges = it)
                }
                CheckboxSetting(
                    label = stringResource(Res.string.notify_mentions),
                    isChecked = user.settings.notifyOnMention
                ) {
                    settings = settings.copy(notifyOnMention = it)
                }
            }
        }
        Row {
            SettingsGroupText(
                text = stringResource(Res.string.star_add)
            )
            Column {
                CheckboxSetting(
                    label = stringResource(Res.string.notify_issue_created),
                    isChecked = user.settings.starOnIssueCreate
                ) {
                    settings = settings.copy(starOnIssueCreate = it)
                }
                CheckboxSetting(
                    label = stringResource(Res.string.notify_issue_updated),
                    isChecked = user.settings.starOnIssueUpdate
                ) {
                    settings = settings.copy(starOnIssueUpdate = it)
                }
                CheckboxSetting(
                    label = stringResource(Res.string.notify_issue_assigned),
                    isChecked = user.settings.starOnIssueAssigned
                ) {
                    settings = settings.copy(starOnIssueAssigned = it)
                }
                CheckboxSetting(
                    label = stringResource(Res.string.notify_issue_upvoted),
                    isChecked = user.settings.starOnIssueUpvote
                ) {
                    settings = settings.copy(starOnIssueUpvote = it)
                }
            }
        }
        Row {
            SettingsGroupText(
                text = stringResource(Res.string.star_remove)
            )
            CheckboxSetting(
                label = stringResource(Res.string.issue_closed),
                isChecked = user.settings.unstarOnIssueClose
            ) {
                settings = settings.copy(unstarOnIssueClose = it)
            }
        }

        Row(
            modifier = Modifier
                .padding(top = paddingMedium)
            ,
        ) {
            FilledActionButton(
                onClick = { onEditUserSettings(settings) },
                text = stringResource(Res.string.save),
                enabled = settings != user.settings,
            )
        }
    }
}

@Composable
private fun SettingsGroupText(
    text: String,
) {
    Text(
        text = text,
        modifier = Modifier
            .requiredWidth(searchBarMaxWidth)
            .requiredHeight(iconContainerSize)
            .wrapContentHeight()
        ,
        style = MaterialTheme.typography.bodyMedium,
    )
}

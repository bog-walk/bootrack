package dev.bogwalk.bootrack.components.issue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import bootrack.composeapp.generated.resources.*
import dev.bogwalk.bootrack.components.utils.avatar
import dev.bogwalk.bootrack.model.*
import dev.bogwalk.bootrack.style.iconSize
import dev.bogwalk.bootrack.style.paddingMedium
import dev.bogwalk.bootrack.style.paddingSmall
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun FieldBox(
    projects: List<Project>,
    users: List<User>,
    issue: IssueDetailed?,
    selectedProject: Project? = null,
    selectedPriority: IssuePriority? = null,
    selectedUser: User? = null,
    selectedState: IssueState? = null,
    selectedLocation: Location? = null,
    modifier: Modifier = Modifier,
    onFieldChangeRequest: (Any) -> Unit
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .clip(MaterialTheme.shapes.small)
            .padding(paddingSmall)
        ,
        verticalArrangement = Arrangement.spacedBy(paddingMedium)
    ) {
        FieldDropDownItem(
            label = stringResource(Res.string.project),
            menuItems = projects.takeIf { issue == null }.orEmpty(),
            currentItem = issue?.let {
                projects.first { p -> p.id == it.issue.projectId }
            } ?: selectedProject!!,
            onMenuItemRequest = { onFieldChangeRequest(it) },
            getItemText = Project::name,
            enabled = projects.isNotEmpty()
        )
        FieldDropDownItem(
            label = stringResource(Res.string.priority),
            menuItems = IssuePriority.entries,
            currentItem = issue?.issue?.priority ?: selectedPriority!!,
            onMenuItemRequest = { onFieldChangeRequest(it) },
            getItemText = IssuePriority::label
        ) {
            PrioritySymbol(
                priority = issue?.issue?.priority ?: selectedPriority!!,
                modifier = Modifier
                    .padding(iconSize - paddingMedium)
            )
        }
        FieldDropDownItem(
            label = stringResource(Res.string.assignee),
            menuItems = users.map { it.fullName },
            currentItem = users.firstOrNull {
                (issue?.issue?.assigneeId ?: selectedUser?.id) == it.id
            }?.fullName
                ?: stringResource(Res.string.unassigned),
            onMenuItemRequest = { onFieldChangeRequest(it) },
            getItemText = { it }
        ) {
            users.firstOrNull { (issue?.issue?.assigneeId ?: selectedUser?.id) == it.id }?.let {
                Icon(
                    imageVector = it.settings.avatar().icon,
                    contentDescription = "",
                    modifier = Modifier
                        .padding(paddingMedium)
                        .requiredSize(iconSize)
                        .clip(CircleShape),
                    tint = it.settings.avatar().tint
                )
            }
        }
        FieldDropDownItem(
            label = stringResource(Res.string.state),
            menuItems = IssueState.entries,
            currentItem = issue?.issue?.state ?: selectedState!!,
            onMenuItemRequest = { onFieldChangeRequest(it) },
            getItemText = IssueState::label
        )

        issue?.let {
            CoupledDecimalField(
                labelA = stringResource(Res.string.latitude),
                itemA = issue.issue.location?.latitude?.truncate() ?: stringResource(Res.string.n_a),
                labelB = stringResource(Res.string.longitude),
                itemB = issue.issue.location?.longitude?.truncate() ?: stringResource(Res.string.n_a),
            )
        } ?: CoupledDecimalTextField(
            labelA = stringResource(Res.string.latitude),
            itemA = selectedLocation?.latitude?.truncate() ?: "",
            labelB = stringResource(Res.string.longitude),
            itemB = selectedLocation?.longitude?.truncate() ?: "",
        ) { lat, long ->
            onFieldChangeRequest(Location(lat.toDouble(), long.toDouble()))
        }
    }
}

private fun Double.truncate(): String = "%.4f".format(this)

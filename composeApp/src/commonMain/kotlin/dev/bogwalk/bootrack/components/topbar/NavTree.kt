package dev.bogwalk.bootrack.components.topbar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import bootrack.composeapp.generated.resources.Res
import bootrack.composeapp.generated.resources.issues
import bootrack.composeapp.generated.resources.project
import bootrack.composeapp.generated.resources.user
import dev.bogwalk.bootrack.components.buttons.InteractionTextButton
import dev.bogwalk.bootrack.components.utils.textButtonColors
import dev.bogwalk.bootrack.style.iconSizeSmall
import dev.bogwalk.bootrack.style.paddingSmall
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun NavTree(
    project: String,
    user: String?,
    issueCount: Int,
    isFiltered: Boolean,
    issueCode: String?,
    onSelectAllIssues: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavText(
            text = if (user == null) {
                stringResource(Res.string.project)
            } else {
                stringResource(Res.string.user)
            }
        )
        BackSlash()
        NavText(text = user ?: project)
        if (user == null) {
            BackSlash()
            InteractionTextButton(
                onClick = onSelectAllIssues,
                text = stringResource(Res.string.issues),
                modifier = Modifier
                    .padding(vertical = paddingSmall),
                style = MaterialTheme.typography.labelLarge,
                colors = textButtonColors(MaterialTheme.colorScheme.onSurface),
            )
            BackSlash()
            if (issueCode == null && issueCount > 0) {
                NavText(text = issueCount.toString())
                if (isFiltered) {
                    Icon(
                        imageVector = Icons.Sharp.FilterList,
                        contentDescription = "",
                        modifier = Modifier
                            .padding(start = paddingSmall)
                            .requiredSize(iconSizeSmall)
                        ,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else if (issueCode != null) {
                NavText(text = issueCode)
            }
        }
    }
}

@Composable
private fun NavText(
    text: String,
) {
    Text(
        text = text,
        modifier = Modifier
            .padding(vertical = paddingSmall)
        ,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.labelLarge
    )
}

@Composable
private fun BackSlash() {
    Text(
        text = "/",
        modifier = Modifier
            .padding(paddingSmall)
        ,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.labelLarge
    )
}

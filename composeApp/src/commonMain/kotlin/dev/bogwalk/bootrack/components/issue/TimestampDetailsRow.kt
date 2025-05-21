package dev.bogwalk.bootrack.components.issue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import bootrack.composeapp.generated.resources.Res
import bootrack.composeapp.generated.resources.created_by
import bootrack.composeapp.generated.resources.last_modified
import dev.bogwalk.bootrack.components.buttons.InteractionTextPopup
import dev.bogwalk.bootrack.components.user.UserProfileSummary
import dev.bogwalk.bootrack.model.User
import dev.bogwalk.bootrack.style.iconContainerSize
import dev.bogwalk.bootrack.style.paddingLarge
import dev.bogwalk.bootrack.style.paddingMedium
import dev.bogwalk.bootrack.style.paddingSmall
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TimestampDetailsRow(
    author: User,
    created: String,
    modified: String,
    timestampsDiffer: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.requiredWidth(iconContainerSize))
        Text(
            text = stringResource(Res.string.created_by),
            modifier = Modifier
                .padding(horizontal = paddingSmall)
            ,
            style = MaterialTheme.typography.titleSmall
        )
        InteractionTextPopup(
            text = author.fullName,
            style = MaterialTheme.typography.titleSmall
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
            text = created,
            modifier = Modifier
                .padding(start = paddingSmall)
            ,
            style = MaterialTheme.typography.titleSmall
        )

        if (timestampsDiffer) {
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(Res.string.last_modified),
                modifier = Modifier
                    .padding(start = paddingLarge, end = paddingSmall)
                ,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = modified,
                modifier = Modifier
                    .padding(start = paddingSmall)
                ,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

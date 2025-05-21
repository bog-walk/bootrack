package dev.bogwalk.bootrack.components.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import dev.bogwalk.bootrack.components.utils.avatar
import dev.bogwalk.bootrack.model.User
import dev.bogwalk.bootrack.style.iconContainerSize
import dev.bogwalk.bootrack.style.paddingMedium
import dev.bogwalk.bootrack.style.paddingSmall

@Composable
internal fun UserProfileSummary(
    user: User,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(paddingSmall)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = user.settings.avatar().icon,
            contentDescription = "",
            modifier = Modifier
                .padding(horizontal = paddingMedium)
                .requiredSize(iconContainerSize)
                .clip(CircleShape)
            ,
            tint = user.settings.avatar().tint
        )
        Column(
            modifier = Modifier
                .padding(end = paddingMedium)
            ,
            verticalArrangement = Arrangement.spacedBy(paddingSmall)
        ) {
            Text(
                text = user.fullName,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = user.username,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

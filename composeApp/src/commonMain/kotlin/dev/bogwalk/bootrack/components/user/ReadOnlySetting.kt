package dev.bogwalk.bootrack.components.user

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import dev.bogwalk.bootrack.style.iconContainerSize
import dev.bogwalk.bootrack.style.paddingMedium
import dev.bogwalk.bootrack.style.paddingSmall
import dev.bogwalk.bootrack.style.searchBarMaxWidth

@Composable
internal fun ReadOnlySetting(
    label: String,
    setting: String? = null,
    icon: ImageVector? = null,
    tint: Color? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier
                .requiredWidth(searchBarMaxWidth)
            ,
            style = MaterialTheme.typography.bodyMedium
        )
        setting?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = "",
                modifier = Modifier
                    .padding(horizontal = paddingSmall, vertical = paddingMedium)
                    .requiredSize(iconContainerSize)
                    .clip(RectangleShape)
                ,
                tint = tint!!
            )
        }
    }
}

package dev.bogwalk.bootrack.components.issue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import dev.bogwalk.bootrack.components.utils.drawBorder
import dev.bogwalk.bootrack.model.IssuePriority
import dev.bogwalk.bootrack.style.*

@Composable
internal fun PrioritySymbol(
    priority: IssuePriority,
    modifier: Modifier = Modifier
) {
    val (darkColor, lightColor) = when (priority) {
        IssuePriority.MINOR -> shadowYellow to neonYellow
        IssuePriority.NORMAL -> shadowBlue to neonBlue
        IssuePriority.MAJOR -> shadowPink to neonOrange
    }
    Text(
        text = "${priority.name.first()}",
        modifier = modifier
            .background(lightColor)
            .drawBehind {
                drawBorder(borderSmall.toPx(), 0f, darkColor, StrokeCap.Butt)
            }
            .requiredSize(iconSizeSmall)
        ,
        color = darkColor,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        lineHeight = 20.sp
    )
}

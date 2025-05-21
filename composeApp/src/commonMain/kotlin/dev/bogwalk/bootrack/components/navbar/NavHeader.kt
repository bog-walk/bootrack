package dev.bogwalk.bootrack.components.navbar

import androidx.compose.animation.core.Transition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import bootrack.composeapp.generated.resources.Res
import bootrack.composeapp.generated.resources.app_logo
import bootrack.composeapp.generated.resources.app_name
import dev.bogwalk.bootrack.screens.NavBarState
import dev.bogwalk.bootrack.style.borderSmall
import dev.bogwalk.bootrack.style.lightGreen
import dev.bogwalk.bootrack.style.paddingMedium
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun NavHeader(
    transition: Transition<NavBarState>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = paddingMedium, end = paddingMedium)
            .drawBehind {
                drawLine(lightGreen, Offset(0f, size.height), Offset(size.width, size.height), borderSmall.toPx(), StrokeCap.Butt)
            }
        ,
    ) {
        NavItem(
            transition = transition,
            label = stringResource(Res.string.app_name),
            icon = painterResource(Res.drawable.app_logo),
            iconDescription = "",
            labelStyle = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterStart),
        )
    }
}

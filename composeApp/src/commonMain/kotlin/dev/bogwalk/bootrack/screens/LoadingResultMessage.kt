package dev.bogwalk.bootrack.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import bootrack.composeapp.generated.resources.Res
import bootrack.composeapp.generated.resources.app_logo
import bootrack.composeapp.generated.resources.duck
import bootrack.composeapp.generated.resources.loading_results
import dev.bogwalk.bootrack.style.borderSmall
import dev.bogwalk.bootrack.style.fieldBoxCompactWidth
import dev.bogwalk.bootrack.style.iconContainerSize
import dev.bogwalk.bootrack.style.paddingLarge
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoadingResultMessage(
    modifier: Modifier = Modifier,
) {
    val delay = 100
    val progressDuration = 3000
    val fadeDuration = progressDuration / 2

    val transition = rememberInfiniteTransition(label = "infinite")

    val ghostHover by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(delay)
        ),
        label = "ghost hover"
    )
    val ghostProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = progressDuration,
                easing = LinearEasing
            ),
            initialStartOffset = StartOffset(delay)
        ),
        label = "ghost progress"
    )
    val ghostFade by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(fadeDuration),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(delay)
        ),
        label = "ghost fade"
    )

    val duckWaddle by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 300,
                easing = EaseInOutCubic,
            ),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(delay)
        ),
        label = "duck waddle"
    )
    val duckProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = progressDuration,
                easing = LinearEasing
            ),
            initialStartOffset = StartOffset(delay)
        ),
        label = "duck progress"
    )
    val duckFade by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(fadeDuration),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(delay)
        ),
        label = "duck fade"
    )

    Column(
        modifier = modifier
            .requiredWidth(fieldBoxCompactWidth)
        ,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.loading_results),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = paddingLarge)
            ,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
            ,
            verticalAlignment = Alignment.Bottom
        ) {
            Icon(
                painter = painterResource(Res.drawable.app_logo),
                contentDescription = "",
                modifier = Modifier
                    .requiredSize(iconContainerSize * 3)
                    .offset(x = -iconContainerSize)
                    .offset {
                        IntOffset(
                            x = (iconContainerSize.toPx() * ghostProgress.dp.toPx()).toInt(),
                            y = (-1 * borderSmall.toPx() * ghostHover.dp.toPx()).toInt()
                        )
                    }
                    .alpha(ghostFade)
                ,
                tint = Color.Unspecified
            )
            Icon(
                painter = painterResource(Res.drawable.duck),
                contentDescription = "",
                modifier = Modifier
                    .requiredSize(iconContainerSize)
                    .offset(x = -iconContainerSize)
                    .offset {
                        IntOffset(
                            x = (iconContainerSize.toPx() * duckProgress.dp.toPx()).toInt(),
                            y = 0
                        )
                    }
                    .rotate(duckWaddle)
                    .alpha(duckFade)
                ,
                tint = Color.Unspecified
            )
        }
    }
}

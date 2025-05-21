package dev.bogwalk.bootrack.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import bootrack.composeapp.generated.resources.Res
import bootrack.composeapp.generated.resources.app_logo_alt
import bootrack.composeapp.generated.resources.coffee
import bootrack.composeapp.generated.resources.no_results
import dev.bogwalk.bootrack.style.iconContainerSize
import dev.bogwalk.bootrack.style.paddingLarge
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun EmptyResultMessage(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
        ,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.no_results),
            modifier = Modifier
                .padding(bottom = paddingLarge)
            ,
            style = MaterialTheme.typography.titleLarge,
        )

        Text(
            text = stringResource(Res.string.coffee),
            modifier = Modifier
                .padding(bottom = paddingLarge)
            ,
            style = MaterialTheme.typography.titleLarge,
        )

        Icon(
            painter = painterResource(Res.drawable.app_logo_alt),
            contentDescription = "",
            modifier = Modifier
                .requiredSize(iconContainerSize * 3)
            ,
            tint = Color.Unspecified
        )
    }
}

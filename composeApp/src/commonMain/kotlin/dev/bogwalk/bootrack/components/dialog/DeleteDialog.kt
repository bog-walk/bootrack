package dev.bogwalk.bootrack.components.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import bootrack.composeapp.generated.resources.Res
import bootrack.composeapp.generated.resources.cancel
import bootrack.composeapp.generated.resources.confirm
import dev.bogwalk.bootrack.components.buttons.FilledActionButton
import dev.bogwalk.bootrack.components.buttons.OutlinedActionButton
import dev.bogwalk.bootrack.components.utils.drawDialogBorder
import dev.bogwalk.bootrack.style.notificationBoxWidth
import dev.bogwalk.bootrack.style.paddingMedium
import dev.bogwalk.bootrack.style.paddingSmall
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DeleteDialog(
    confirmation: String,
    message: String,
    onCancelDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(paddingSmall)
            .requiredWidth(notificationBoxWidth)
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                drawDialogBorder()
            }
            .padding(paddingSmall)
        ,
    ) {
        Text(
            text = confirmation,
            modifier = Modifier
                .padding(horizontal = paddingMedium, vertical = paddingSmall)
            ,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = message,
            modifier = Modifier
                .padding(horizontal = paddingMedium, vertical = paddingSmall)
            ,
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = paddingSmall)
            ,
            horizontalArrangement = Arrangement.End
        ) {
            FilledActionButton(
                onClick = onConfirmDelete,
                text = stringResource(Res.string.confirm),
            )
            OutlinedActionButton(
                onClick = onCancelDelete,
                text = stringResource(Res.string.cancel)
            )
        }
    }
}

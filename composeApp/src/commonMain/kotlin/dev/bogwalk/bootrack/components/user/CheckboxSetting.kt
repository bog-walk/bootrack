package dev.bogwalk.bootrack.components.user

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.bogwalk.bootrack.style.lightGrey
import dev.bogwalk.bootrack.style.paddingSmall

@Composable
internal fun CheckboxSetting(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    var checked by remember { mutableStateOf(isChecked) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = {
                checked = it
                onCheckedChange(it)
            },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.tertiary,
                uncheckedColor = lightGrey,
                checkmarkColor = MaterialTheme.colorScheme.onTertiary,
            )
        )
        Text(
            text = label,
            modifier = Modifier
                .padding(start = paddingSmall)
            ,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

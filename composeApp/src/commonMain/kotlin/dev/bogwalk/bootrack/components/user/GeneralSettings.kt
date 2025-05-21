package dev.bogwalk.bootrack.components.user

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import bootrack.composeapp.generated.resources.*
import dev.bogwalk.bootrack.components.buttons.FilledActionButton
import dev.bogwalk.bootrack.components.utils.TextFieldDefaultDecorationBox
import dev.bogwalk.bootrack.components.utils.avatar
import dev.bogwalk.bootrack.components.utils.drawBorder
import dev.bogwalk.bootrack.model.*
import dev.bogwalk.bootrack.style.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun GeneralSettings(
    user: User,
    projects: List<Project>,
    onEditUserSettings: (UserSettings) -> Unit,
) {
    var settings by remember { mutableStateOf(user.settings) }
    var maxTravel by remember { mutableStateOf(user.settings.maxTravelDistance.toString()) }
    var selectedSort by remember { mutableStateOf(user.settings.defaultSort) }

    val travelInteractionSource = remember { MutableInteractionSource() }
    val isFocused by travelInteractionSource.collectIsFocusedAsState()

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .padding(start = paddingLarge, top = paddingLarge, bottom = paddingLarge)
            .onPointerEvent(PointerEventType.Press) {
                focusManager.clearFocus()
            }
        ,
        verticalArrangement = Arrangement.spacedBy(paddingLarge)
    ) {
        ReadOnlySetting(
            label = stringResource(Res.string.user_full_name),
            setting = user.fullName,
        )
        ReadOnlySetting(
            label = stringResource(Res.string.user_name),
            setting = user.username,
        )
        ReadOnlySetting(
            label = stringResource(Res.string.user_avatar),
            icon = user.settings.avatar().icon,
            tint = user.settings.avatar().tint
        )
        DropDownSetting(
            label = stringResource(Res.string.user_default_project),
            menuItems = projects,
            currentItem = user.settings.defaultProject,
            onMenuItemRequest = { settings = settings.copy(defaultProject = it) },
            getItemText = Project::name,
        )
        ReadOnlySetting(
            label = stringResource(Res.string.user_default_location),
            setting = user.settings.location.toString(),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.user_max_travel),
                modifier = Modifier
                    .requiredWidth(searchBarMaxWidth)
                ,
                style = MaterialTheme.typography.bodyMedium
            )
            BasicTextField(
                value = maxTravel,
                onValueChange = { value -> maxTravel = value.takeWhile { it.isDigit() } },
                modifier = Modifier
                    .padding(top = paddingSmall, end = paddingMedium, bottom = paddingSmall)
                    .drawBehind {
                        if (isFocused) {
                            drawBorder(borderSmall.toPx(), 0f, shadowGreen, StrokeCap.Butt)
                        }
                    }
                ,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = neonBlue),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                interactionSource = travelInteractionSource
            ) { innerTextField ->
                TextFieldDefaultDecorationBox(
                    text = maxTravel,
                    innerTextField = innerTextField,
                    singleLine = true,
                    interactionSource = travelInteractionSource,
                    horizontalPadding = 0.dp,
                    verticalPadding = 0.dp,
                )
            }
        }
        DropDownSetting(
            label = stringResource(Res.string.user_dt_format),
            menuItems = UserDateFormat.entries.map { it.pattern },
            currentItem = user.settings.dateFormat,
            onMenuItemRequest = { settings = settings.copy(dateFormat = it) },
            getItemText = { it },
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.user_sort),
                modifier = Modifier
                    .requiredWidth(searchBarMaxWidth)
                ,
                style = MaterialTheme.typography.bodyMedium
            )
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .padding(top = paddingSmall, end = paddingMedium, bottom = paddingSmall)
                ,
            ) {
                UserSort.entries.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = option == selectedSort,
                        onClick = {
                            selectedSort = option
                            settings = settings.copy(defaultSort = selectedSort)
                        },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = UserSort.entries.size,
                            baseShape = MaterialTheme.shapes.small
                        ),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.tertiary,
                            activeContentColor = MaterialTheme.colorScheme.onTertiary,
                            activeBorderColor = MaterialTheme.colorScheme.tertiary,
                            inactiveBorderColor = MaterialTheme.colorScheme.tertiary,
                        ),
                        label = {
                            Text(
                                text = option.label,
                                color = if (option == selectedSort) {
                                    MaterialTheme.colorScheme.onTertiary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(top = paddingMedium)
            ,
        ) {
            FilledActionButton(
                onClick = {
                    val updatedSettings = settings.copy(
                        maxTravelDistance = maxTravel.toInt(),
                    )
                    onEditUserSettings(updatedSettings)
                },
                text = stringResource(Res.string.save),
                enabled = maxTravel.toInt() != settings.maxTravelDistance || settings != user.settings,
            )
        }
    }
}

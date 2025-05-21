package dev.bogwalk.bootrack.components.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import dev.bogwalk.bootrack.model.UserSettings

internal data class UserAvatar(
    val icon: ImageVector,
    val tint: Color,
)

internal fun UserSettings.avatar(): UserAvatar {
    return UserAvatar(iconOptions[avatarIcon], tintOptions[avatarTint])
}

internal val iconOptions = listOf(
    Icons.Sharp.Face2,
    Icons.Sharp.Face3,
    Icons.Sharp.Face4,
    Icons.Sharp.Face5,
    Icons.Sharp.Face6,
    Icons.Sharp.Face
)

internal val tintOptions = listOf(
    Color(0xffcc98fc), // pastel purple
    Color(0xfffcdc98), // pastel yellow
    Color(0xff98d6fc), // pastel blue
    Color(0xffa9fc98), // pastel green
    Color(0xfffcb698), // pastel orange
    Color(0xfffc98a5), // burn red
)

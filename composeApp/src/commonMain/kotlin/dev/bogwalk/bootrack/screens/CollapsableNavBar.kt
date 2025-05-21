package dev.bogwalk.bootrack.screens

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.sharp.KeyboardDoubleArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.zIndex
import bootrack.composeapp.generated.resources.*
import dev.bogwalk.bootrack.components.buttons.ElevatedIconButton
import dev.bogwalk.bootrack.components.navbar.NavButton
import dev.bogwalk.bootrack.components.navbar.NavDropdownMenu
import dev.bogwalk.bootrack.components.navbar.NavHeader
import dev.bogwalk.bootrack.components.utils.drawBorderWithShadow
import dev.bogwalk.bootrack.model.Project
import dev.bogwalk.bootrack.model.SessionNotification
import dev.bogwalk.bootrack.model.User
import dev.bogwalk.bootrack.style.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

internal enum class NavBarState { EXPANDED, COLLAPSED }

@Composable
fun CollapsableNavBar(
    projects: List<Project>,
    currentProject: Project?,
    currentUser: User?,
    unReadNotifications: Boolean,
    notifications: List<SessionNotification>,
    onOpenDashboardRequest: () -> Unit,
    onOpenUserAccountRequest: () -> Unit,
    onToggleNotification: (SessionNotification) -> Unit,
    onShowIssue: (String) -> Unit,
    onChangeProjectRequest: (Project) -> Unit
) {
    var navState by remember { mutableStateOf(NavBarState.EXPANDED) }

    val transition = updateTransition(navState, label = "nav state")
    val columnWidth by transition.animateDp(
        transitionSpec = {
            when {
                NavBarState.COLLAPSED isTransitioningTo NavBarState.EXPANDED -> spring()
                else -> tween()
            }
        },
        label = "nav width"
    ) { state ->
        when (state) {
            NavBarState.COLLAPSED -> navBarRail
            NavBarState.EXPANDED -> navBarWide
        }
    }

    Column(
        modifier = Modifier
            .width(columnWidth)
            .fillMaxHeight()
            .padding(start = paddingSmall, top = paddingSmall, end = paddingSmall / 2, bottom = paddingSmall)
            .drawBehind {
                drawBorderWithShadow(clippedCorners = true)
            }
            .zIndex(9f)
        ,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        NavHeader(
            transition = transition,
        )

        currentProject?.let {
            NavDropdownMenu(
                label = stringResource(Res.string.issues),
                icon = painterResource(Res.drawable.skull),
                transition = transition,
                menuItems = projects,
                currentItem = it,
                onMenuItemRequest = onChangeProjectRequest,
                getItemText = Project::name
            )
        } ?: NavButton(
            onClick = {},
            transition = transition,
            label = stringResource(Res.string.issues),
            icon = painterResource(Res.drawable.skull),
            enabled = false,
        )

        NavButton(
            onClick = onOpenDashboardRequest,
            transition = transition,
            label = stringResource(Res.string.dashboard),
            icon = painterResource(Res.drawable.radar),
            enabled = currentUser != null,
        )

        Spacer(Modifier.weight(0.5f))

        NavPopup(
            transition = transition,
            label = stringResource(Res.string.notifications),
            notifications = notifications,
            icon = painterResource(if (unReadNotifications) Res.drawable.bell_on else Res.drawable.bell_off),
            enabled = currentUser != null && notifications.isNotEmpty(),
            onToggleRead = onToggleNotification,
            onShowIssue = onShowIssue
        )

        NavButton(
            onClick = currentUser?.let { onOpenUserAccountRequest } ?: {},
            transition = transition,
            label = currentUser?.fullName ?: stringResource(Res.string.log_in),
            icon = currentUser?.let { painterResource(Res.drawable.face) } ?: painterResource(Res.drawable.account),
            enabled = currentUser != null,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = paddingMedium)
                .offset(x = paddingLarge)
            ,
            contentAlignment = Alignment.CenterEnd
        ) {
            ElevatedIconButton(
                onClick = {
                    navState = when (navState) {
                        NavBarState.COLLAPSED -> NavBarState.EXPANDED
                        NavBarState.EXPANDED -> NavBarState.COLLAPSED
                    }
                },
                icon = if (navState == NavBarState.EXPANDED) {
                    Icons.Sharp.KeyboardDoubleArrowLeft
                } else {
                    Icons.Sharp.KeyboardDoubleArrowRight
                }
            )
        }
    }
}

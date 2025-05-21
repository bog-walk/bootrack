package dev.bogwalk.bootrack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import bootrack.composeapp.generated.resources.Res
import bootrack.composeapp.generated.resources.delete_issue
import bootrack.composeapp.generated.resources.delete_issue_confirm
import dev.bogwalk.bootrack.client.MainViewModel
import dev.bogwalk.bootrack.client.state.MainScreenState
import dev.bogwalk.bootrack.client.state.MainUiState
import dev.bogwalk.bootrack.client.storage.AppCache
import dev.bogwalk.bootrack.components.dialog.DeleteDialog
import dev.bogwalk.bootrack.components.dialog.NewIssueDialog
import dev.bogwalk.bootrack.components.utils.drawGridSurface
import dev.bogwalk.bootrack.screens.CollapsableNavBar
import dev.bogwalk.bootrack.screens.DynamicTopBar
import dev.bogwalk.bootrack.screens.MainScreen
import dev.bogwalk.bootrack.style.iconContainerSizeSmall
import dev.bogwalk.bootrack.style.notificationBoxWidth
import dev.bogwalk.bootrack.style.paddingSmall
import org.jetbrains.compose.resources.stringResource

@Composable
fun FrameWindowScope.App(
    api: MainViewModel,
    cache: AppCache,
) {
    val mainState by api.mainState.collectAsState()
    val issueState by api.issueState.collectAsState()

    val users by cache.getUsers().collectAsState()
    val projects by cache.getProjects().collectAsState()
    val issues by cache.getIssuesSummary().collectAsState()
    val rankedIssues by cache.getRankedIssues().collectAsState()
    val notifications by cache.getNotifications().collectAsState()

    var dialogMode by remember { mutableStateOf(AppDialog.NONE) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawGridSurface(
                    lighter = mainState.mainScreenState() == MainScreenState.ISSUES_RADAR
                )
            }
    ) {
        CollapsableNavBar(
            projects = projects,
            currentProject = if (mainState is MainUiState.LoggedIn) mainState.currentProject() else null,
            currentUser = if (mainState is MainUiState.LoggedIn) mainState.currentUser() else null,
            unReadNotifications = mainState.hasUnreadNotifications(),
            notifications = notifications,
            onOpenDashboardRequest = api::showIssueRadar,
            onOpenUserAccountRequest = api::showUserAccount,
            onToggleNotification = api::updateNotification,
            onShowIssue = api::showIssue,
            onChangeProjectRequest = api::loadIssuesByProject
        )

        BoxWithConstraints (
            contentAlignment = Alignment.Center
        ) {
            Column {
                DynamicTopBar(
                    state = mainState.mainScreenState(),
                    project = if (mainState is MainUiState.LoggedIn) mainState.currentProject().name else null,
                    user = if (mainState.mainScreenState() == MainScreenState.USER_ACCOUNT) mainState.currentUser().fullName else null,
                    issueCount = mainState.issueCount(),
                    issueCode = issueState.currentIssue()?.issue?.code,
                    issueIndex = issueState.currentIssueIndex(),
                    onBackRequest = api::handleBackButton,
                    onPreviousIssueRequest = api::showPreviousIssue,
                    onNextIssueRequest = api::showNextIssue,
                    onAddNewIssueRequest = { dialogMode = AppDialog.NEW_ISSUE },
                    onQueryIssuesRequest = { search -> api.filterIssues(mainState.currentProject(), search, false) },
                    onFilterIssuesRequest = { search -> api.filterIssues(mainState.currentProject(), search, true) },
                    onShowAllIssues = api::handleBackButton,
                )

                MainScreen(
                    mainState = mainState,
                    issueState = issueState,
                    users = users,
                    projects = projects,
                    issues = issues,
                    rankedIssues = rankedIssues,
                    onRefreshRanking = api::rankIssues,
                    onEditUserSettings = api::editUserSettings,
                    onShowIssueRequest = api::showIssue,
                    onLoadMoreRequest = api::loadMoreIssues,
                    onLoadPageRequest = api::loadIssuesByPage,
                    onWatchIssueRequest = { issue, code, on ->
                        api.updateIssue(mainState.currentUser().id, "stars", issue)
                    },
                    onUpvoteIssueRequest = { issue, code, on ->
                        val user = mainState.currentUser()
                        if (user.settings.starOnIssueUpvote && on && user.id !in issue.watchers) {
                            api.updateIssue(user.id, "upvotes", issue.copy(watchers = issue.watchers + user.id))
                        } else {
                            api.updateIssue(user.id, "upvotes", issue)
                        }
                    },
                    onEditIssueRequest = api::updateIssue,
                    onDeleteIssueRequest = { dialogMode = AppDialog.DELETE_ISSUE },
                    onAddCommentRequest = api::addComment,
                    onEditCommentRequest = api::editComment,
                    onDeleteCommentRequest = api::deleteComment
                )
            }

            if (dialogMode != AppDialog.NONE) {
                Box( // overlay between dialog & current screen
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = paddingSmall / 2, top = paddingSmall, end = paddingSmall, bottom = paddingSmall)
                        .alpha(0.7f)
                        .background(MaterialTheme.colorScheme.background)
                )

                val (position, size) = this@App.centerInMainScreen(
                    mainScope = this,
                    width = if (dialogMode == AppDialog.DELETE_ISSUE) notificationBoxWidth else null
                )

                DialogWindow(
                    onCloseRequest = { dialogMode = AppDialog.NONE },
                    state = rememberDialogState(position = position, size = size),
                    undecorated = true,
                    transparent = true,
                    resizable = false,
                ) {
                    when (dialogMode) {
                        AppDialog.NEW_ISSUE -> NewIssueDialog(
                            projects = projects,
                            currentProject = mainState.currentProject(),
                            users = users,
                            currentUser = mainState.currentUser(),
                            onCancelNewIssue = { dialogMode = AppDialog.NONE },
                            onAddNewIssueRequest = { newIssue ->
                                dialogMode = AppDialog.NONE
                                api.createIssue(newIssue)
                            }
                        )
                        AppDialog.DELETE_ISSUE -> DeleteDialog(
                            confirmation = stringResource(Res.string.delete_issue_confirm),
                            message = stringResource(Res.string.delete_issue),
                            onCancelDelete = { dialogMode = AppDialog.NONE },
                            onConfirmDelete = {
                                dialogMode = AppDialog.NONE
                                issueState.currentIssue()?.let {
                                    api.removeIssue(it)
                                } ?: error("Delete can only be called on a current detailed issue")
                            }
                        )
                        AppDialog.NONE -> {}
                    }
                }
            }
        }
    }
}

private enum class AppDialog {
    NONE,
    NEW_ISSUE,
    DELETE_ISSUE,
}

@Composable
private fun FrameWindowScope.centerInMainScreen(
    mainScope: BoxWithConstraintsScope,
    width: Dp?,
): Pair<WindowPosition, DpSize> {
    val dialogWidth = width ?: (mainScope.maxWidth * 0.75f)
    val dialogHeight = mainScope.maxHeight * 0.75f
    val dialogSize = DpSize(dialogWidth, dialogHeight)

    val mainXOffset = (mainScope.maxWidth - dialogWidth) / 2
    val mainYOffset = (mainScope.maxHeight - dialogHeight) / 2

    val navBarXOffset = this.window.width.dp - mainScope.maxWidth - iconContainerSizeSmall
    val paddingYOffset = -iconContainerSizeSmall

    val dialogPosition = this.window.locationOnScreen.let { locationOnScreen ->
        WindowPosition.Absolute(
            x = (locationOnScreen.x.dp + mainXOffset + navBarXOffset),
            y = (locationOnScreen.y.dp + mainYOffset - paddingYOffset)
        )
    }

    return dialogPosition to dialogSize
}

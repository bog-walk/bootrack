package dev.bogwalk.bootrack.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import bootrack.composeapp.generated.resources.Res
import bootrack.composeapp.generated.resources.general
import bootrack.composeapp.generated.resources.notifications
import bootrack.composeapp.generated.resources.saved_search
import dev.bogwalk.bootrack.client.state.IssueUiState
import dev.bogwalk.bootrack.components.user.GeneralSettings
import dev.bogwalk.bootrack.components.user.NotificationSettings
import dev.bogwalk.bootrack.components.user.RankedIssueList
import dev.bogwalk.bootrack.components.utils.UserRankBy
import dev.bogwalk.bootrack.model.*
import dev.bogwalk.bootrack.style.paddingLarge
import dev.bogwalk.bootrack.style.paddingSmall
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAccount(
    issueState: IssueUiState,
    user: User,
    users: List<User>,
    projects: List<Project>,
    rankedIssues: List<List<IssueSummarized>>,
    currentRankBy: UserRankBy,
    onEditUserSettings: (UserSettings) -> Unit,
    onRefreshRanking: (UserRankBy) -> Unit,
    onShowIssueRequest: (IssueSummarized) -> Unit,
    onWatchIssueRequest: (Issue, String, Boolean) -> Unit,
    onUpvoteIssueRequest: (Issue, String, Boolean) -> Unit,
) {
    var selectedTab by remember { mutableStateOf(UserTab.GENERAL) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingSmall)
            .background(MaterialTheme.colorScheme.background)
        ,
    ) {
        PrimaryTabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingLarge)
            ,
            contentColor = Color.Transparent,
            containerColor = Color.Transparent,
        ) {
            UserTab.entries.forEach { tab ->
                Tab(
                    selected = tab.ordinal == selectedTab.ordinal,
                    onClick = {
                        selectedTab = tab
                    },
                    text = {
                        Text(
                            text = tab.getLabel(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    selectedContentColor = Color.Transparent,
                    unselectedContentColor = Color.Transparent,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
            ,
        ) {
            handleIssueStateOrShowContent(issueState) {
                when (selectedTab) {
                    UserTab.GENERAL -> GeneralSettings(
                        user = user,
                        projects = projects,
                        onEditUserSettings = onEditUserSettings
                    )

                    UserTab.NOTIFICATIONS -> NotificationSettings(
                        user = user,
                        onEditUserSettings = onEditUserSettings
                    )

                    UserTab.SAVED_SEARCH -> RankedIssueList(
                        user = user,
                        users = users,
                        issues = rankedIssues,
                        currentRankBy = currentRankBy,
                        onRefreshSearch = onRefreshRanking,
                        onShowIssueRequest = onShowIssueRequest,
                        onWatchIssueRequest = onWatchIssueRequest,
                        onUpvoteIssueRequest = onUpvoteIssueRequest,
                    )
                }
            }
        }
    }
}

private enum class UserTab {
    GENERAL,
    NOTIFICATIONS,
    SAVED_SEARCH
}

@Composable
private fun UserTab.getLabel(): String = when (this) {
    UserTab.GENERAL -> stringResource(Res.string.general)
    UserTab.NOTIFICATIONS -> stringResource(Res.string.notifications)
    UserTab.SAVED_SEARCH -> stringResource(Res.string.saved_search)
}

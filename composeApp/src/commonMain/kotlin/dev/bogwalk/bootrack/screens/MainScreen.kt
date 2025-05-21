package dev.bogwalk.bootrack.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import dev.bogwalk.bootrack.client.state.IssueUiState
import dev.bogwalk.bootrack.client.state.MainScreenState
import dev.bogwalk.bootrack.client.state.MainUiState
import dev.bogwalk.bootrack.components.utils.UserRankBy
import dev.bogwalk.bootrack.components.utils.avatar
import dev.bogwalk.bootrack.components.utils.drawBorderWithShadow
import dev.bogwalk.bootrack.model.*
import dev.bogwalk.bootrack.style.paddingLarge
import dev.bogwalk.bootrack.style.paddingSmall

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainState: MainUiState,
    issueState: IssueUiState,
    users: List<User>,
    projects: List<Project>,
    issues: List<IssueSummarized>,
    rankedIssues: List<List<IssueSummarized>>,
    onRefreshRanking: (UserRankBy) -> Unit,
    onEditUserSettings: (UserSettings) -> Unit,
    onShowIssueRequest: (IssueSummarized) -> Unit,
    onLoadMoreRequest: () -> Unit,
    onLoadPageRequest: (Int) -> Unit,
    onWatchIssueRequest: (Issue, String, Boolean) -> Unit,
    onUpvoteIssueRequest: (Issue, String, Boolean) -> Unit,
    onEditIssueRequest: (IssueDetailed) -> Unit,
    onDeleteIssueRequest: (IssueDetailed) -> Unit,
    onAddCommentRequest: (Comment) -> Unit,
    onEditCommentRequest: (Comment) -> Unit,
    onDeleteCommentRequest: (Comment) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = paddingSmall / 2, top = paddingSmall, bottom = paddingSmall, end = paddingSmall)
            .drawBehind {
                drawBorderWithShadow()
            }
        ,
    ) {
        when (mainState.mainScreenState()) {
            MainScreenState.ISSUES_LIST, MainScreenState.FILTERED_ISSUES_LIST -> {
                handleIssueStateOrShowContent(issueState) {
                    LazyColumnList(
                        issues = issues,
                        users = users,
                        userId = mainState.currentUser().id,
                        scrollState = issueState.issueScrollState(),
                        loadState = issueState.issueLoadState(),
                        onLoadMoreRequest = onLoadMoreRequest,
                        onLoadPageRequest = onLoadPageRequest,
                        onShowIssueRequest = onShowIssueRequest,
                        onWatchIssueRequest = onWatchIssueRequest,
                        onUpvoteIssueRequest = onUpvoteIssueRequest,
                    )
                }
            }

            MainScreenState.ISSUE_DETAILS -> {
                handleIssueStateOrShowContent(issueState) {
                    IssueScreen(
                        projects = projects,
                        users = users,
                        issue = issueState.currentIssue()!!,
                        user = mainState.currentUser(),
                        onWatchIssueRequest = onWatchIssueRequest,
                        onUpvoteIssueRequest = onUpvoteIssueRequest,
                        onEditIssueRequest = onEditIssueRequest,
                        onDeleteIssueRequest = onDeleteIssueRequest,
                        onAddCommentRequest = onAddCommentRequest,
                        onEditCommentRequest = onEditCommentRequest,
                        onDeleteCommentRequest = onDeleteCommentRequest,
                    )
                }
            }

            MainScreenState.ISSUES_RADAR -> {
                handleIssueStateOrShowContent(issueState) {
                    IssueRadar(
                        icon = mainState.currentUser().settings.avatar().icon,
                        tint = mainState.currentUser().settings.avatar().tint,
                        userLocation = mainState.currentUser().settings.location,
                        issues = issues,
                        onShowIssueRequest = onShowIssueRequest,
                    )
                }
            }

            MainScreenState.USER_ACCOUNT -> UserAccount(
                issueState = issueState,
                user = mainState.currentUser(),
                users = users,
                projects = projects,
                rankedIssues = rankedIssues,
                currentRankBy = mainState.currentSavedRank(),
                onEditUserSettings = onEditUserSettings,
                onRefreshRanking = onRefreshRanking,
                onShowIssueRequest = onShowIssueRequest,
                onWatchIssueRequest = onWatchIssueRequest,
                onUpvoteIssueRequest = onUpvoteIssueRequest,
            )
        }
    }
}

@Composable
internal fun BoxScope.handleIssueStateOrShowContent(
    state: IssueUiState,
    content: @Composable (BoxScope.() -> Unit)
) {
    when (state) {
        IssueUiState.Loading -> LoadingResultMessage(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = -paddingLarge)
        )

        IssueUiState.NoResult -> EmptyResultMessage(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = -paddingLarge)
        )

        IssueUiState.Error -> ErrorResultMessage(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = -paddingLarge)
        )

        is IssueUiState.Content -> content()
    }
}
package dev.bogwalk.bootrack.client.state

import dev.bogwalk.bootrack.components.utils.UserRankBy
import dev.bogwalk.bootrack.model.Project
import dev.bogwalk.bootrack.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

sealed class MainUiState {
    data object LoggedOut : MainUiState()

    data class LoggedIn(
        val mainScreenState: MainScreenState,
        val currentUser: User,
        val currentProject: Project,
        val issueCount: Int,
        val currentSavedRank: UserRankBy,
        val hasUnreadNotifications: Boolean,
    ) : MainUiState()

    fun mainScreenState(): MainScreenState = (this as? LoggedIn)?.mainScreenState
        ?: MainScreenState.ISSUES_LIST

    fun currentUser(): User = (this as? LoggedIn)?.currentUser
        ?: error("$INVALID_STATE_MESSAGE currentUser: $this")

    fun currentProject(): Project = (this as? LoggedIn)?.currentProject
        ?: error("$INVALID_STATE_MESSAGE currentProject: $this")

    fun issueCount(): Int = (this as? LoggedIn)?.issueCount
        ?: 0

    fun currentSavedRank(): UserRankBy = (this as? LoggedIn)?.currentSavedRank
        ?: UserRankBy.OPEN

    fun hasUnreadNotifications(): Boolean = (this as? LoggedIn)?.hasUnreadNotifications == true

    companion object {
        private const val INVALID_STATE_MESSAGE = "Current state does not allow access to"
    }
}

enum class MainScreenState  {
    ISSUES_LIST,
    ISSUE_DETAILS,
    ISSUES_RADAR,
    FILTERED_ISSUES_LIST,
    USER_ACCOUNT,
}

internal fun MutableStateFlow<MainUiState>.updateContent(
    body: (MainUiState.LoggedIn) -> MainUiState.LoggedIn
) {
    this.update { oldState ->
        (oldState as? MainUiState.LoggedIn)
            ?.let { body(it) }
            ?: error("Current state does not allow update of content: $oldState")
    }
}

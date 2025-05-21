package dev.bogwalk.bootrack.client.state

import dev.bogwalk.bootrack.model.IssueDetailed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

sealed class IssueUiState {
    data object Loading : IssueUiState()

    data object NoResult : IssueUiState()

    data object Error : IssueUiState()

    data class Content(
        val issueScrollState: ListScrollPosition,
        val issueLoadState: ListLoadState,
        val filterCondition: FilterCondition?,
        val currentIssue: IssueDetailed?,
        val currentIssueIndex: Int?,
    ) : IssueUiState()

    fun issueScrollState(): ListScrollPosition = (this as? Content)?.issueScrollState
        ?: error("$INVALID_STATE_MESSAGE issueScrollState: $this")

    fun issueLoadState(): ListLoadState = (this as? Content)?.issueLoadState
        ?: error("$INVALID_STATE_MESSAGE issueLoadState: $this")

    fun filterCondition(): FilterCondition? = (this as? Content)?.filterCondition

    fun currentIssue(): IssueDetailed? = (this as? Content)?.currentIssue

    fun currentIssueIndex(): Int? = (this as? Content)?.currentIssueIndex

    companion object {
        private const val INVALID_STATE_MESSAGE = "Current state does not allow access to"
    }
}

data class ListScrollPosition(
    val firstVisibleItemIndex: Int,
    val firstVisibleItemScrollOffset: Int,
)

data class ListLoadState(
    val totalPageCount: Int,
    val currentPageIndex: Int?,
    val canLoadMore: Boolean,
)

data class FilterCondition(
    val searchText: String,
    val hideResolved: Boolean,
)

internal fun MutableStateFlow<IssueUiState>.updateContent(
    body: (IssueUiState.Content) -> IssueUiState.Content
) {
    this.update { oldState ->
        (oldState as? IssueUiState.Content)
            ?.let { body(it) }
            ?: error("Current state does not allow update of content: $oldState")
    }
}

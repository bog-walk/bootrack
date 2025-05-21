package dev.bogwalk.bootrack.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import bootrack.composeapp.generated.resources.Res
import bootrack.composeapp.generated.resources.load_more
import dev.bogwalk.bootrack.client.state.ListLoadState
import dev.bogwalk.bootrack.client.state.ListScrollPosition
import dev.bogwalk.bootrack.components.buttons.OutlinedActionButton
import dev.bogwalk.bootrack.components.buttons.PaginationGroup
import dev.bogwalk.bootrack.components.issue.LazyListScrollbar
import dev.bogwalk.bootrack.components.issue.SummaryCard
import dev.bogwalk.bootrack.model.Issue
import dev.bogwalk.bootrack.model.IssueSummarized
import dev.bogwalk.bootrack.model.User
import dev.bogwalk.bootrack.style.iconContainerSize
import dev.bogwalk.bootrack.style.paddingLarge
import dev.bogwalk.bootrack.style.paddingMedium
import dev.bogwalk.bootrack.style.paddingSmall
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun LazyColumnList(
    issues: List<IssueSummarized>,
    users: List<User>,
    userId: Int,
    scrollState: ListScrollPosition,
    loadState: ListLoadState,
    onLoadMoreRequest: () -> Unit,
    onLoadPageRequest: (Int) -> Unit,
    onShowIssueRequest: (IssueSummarized) -> Unit,
    onWatchIssueRequest: (Issue, String, Boolean) -> Unit,
    onUpvoteIssueRequest: (Issue, String, Boolean) -> Unit,
) {
    val lazyScrollState = rememberLazyListState(
        scrollState.firstVisibleItemIndex,
        scrollState.firstVisibleItemScrollOffset
    )
    val lazyScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        lazyScrollState.animateScrollToItem(
            lazyScrollState.firstVisibleItemIndex,
            lazyScrollState.firstVisibleItemScrollOffset
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = paddingSmall)
        ,
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
            ,
            state = lazyScrollState,
            contentPadding = PaddingValues(vertical = paddingMedium, horizontal = paddingLarge),
            verticalArrangement = TopWithFooterArrangement,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = issues,
                key = { it.issue.code },
            ) { issue ->
                SummaryCard(
                    issue = issue,
                    user = users.first { it.id == userId },
                    author = users.first { it.id == issue.issue.authorId },
                    assignee = issue.issue.assigneeId?.let { id -> users.firstOrNull { it.id == id } },
                    onShowIssueRequest = onShowIssueRequest,
                    onWatchIssueRequest = onWatchIssueRequest,
                    onUpvoteIssueRequest = onUpvoteIssueRequest,
                )
            }

            if (loadState.currentPageIndex != null) {
                item(
                    key = "loading_by_page",
                ) {
                    PaginationGroup(
                        onChangePageRequest = {
                            onLoadPageRequest(it)
                            lazyScope.launch {
                                lazyScrollState.scrollToItem(0)
                            }
                        },
                        pageIndex = loadState.currentPageIndex,
                        pageCount = loadState.totalPageCount,
                        modifier = Modifier
                            .padding(top = paddingLarge)
                            .align(Alignment.BottomCenter)
                    )
                }
            } else if (loadState.canLoadMore) {
                item(
                    key = "loading_more",
                ) {
                    OutlinedActionButton(
                        onClick = onLoadMoreRequest,
                        text = stringResource(Res.string.load_more),
                        modifier = Modifier
                            .padding(top = paddingLarge)
                        ,
                    )
                }
            } else {
                // otherwise final list item will be rooted at footer
                item(
                    key = "placeholder"
                ) {
                    Spacer(Modifier.height(iconContainerSize))
                }
            }
        }

        LazyListScrollbar(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(vertical = paddingSmall)
                .offset(x = -paddingMedium)
            ,
            state = lazyScrollState,
        )
    }
}

private object TopWithFooterArrangement : Arrangement.Vertical {
    override fun Density.arrange(
        totalSize: Int,
        sizes: IntArray,
        outPositions: IntArray
    ) {
        var occupied = 0
        sizes.forEachIndexed { index, size ->
            outPositions[index] = occupied
            occupied += size
        }
        if (occupied < totalSize) {
            val lastIndex = outPositions.lastIndex
            outPositions[lastIndex] = totalSize - sizes.last()
        }
    }
}

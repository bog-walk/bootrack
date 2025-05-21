package dev.bogwalk.bootrack.client

import dev.bogwalk.bootrack.client.state.*
import dev.bogwalk.bootrack.client.storage.AppCache
import dev.bogwalk.bootrack.components.utils.DummyValues
import dev.bogwalk.bootrack.components.utils.UserRankBy
import dev.bogwalk.bootrack.components.utils.getNewMentions
import dev.bogwalk.bootrack.model.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.sign

class MainViewModel(
    private val client: AppClient,
    private val cache: AppCache,
    private val scope: CoroutineScope,
) {
    private val mState = MutableStateFlow<MainUiState>(MainUiState.LoggedOut)
    val mainState = mState.asStateFlow()

    private val iState = MutableStateFlow<IssueUiState>(IssueUiState.Loading)
    val issueState = iState.asStateFlow()

    // Set 'true' for offset pagination; 'false' for cursor pagination
    private val usePagination = true

    // project-level

    /**
     * Loads data for users, projects, issues, and notifications in the default project (specified by current user)
     * and propagates this initial data to the local caches.
     */
    fun loadInitialState() {
        scope.launch {
            // already in load state
            try {
                val loadedUsers = client.getAllUsers()
                cache.setUsers { loadedUsers }

                val loadedProjects = client.getAllProjects()
                cache.setProjects { loadedProjects }

                // currently always 1 main user since login/auth not set up
                val loadedNotifications = client.getNotificationsByReceiver(U1.id)
                cache.setNotifications { loadedNotifications }

                mState.value = MainUiState.LoggedIn(
                    mainScreenState = MainScreenState.ISSUES_LIST,
                    currentUser = U1,
                    currentProject = U1.settings.defaultProject,
                    issueCount = 0,
                    currentSavedRank = UserRankBy.OPEN,
                    hasUnreadNotifications = cache.getNotifications().value.any { !it.isRead },
                )

                loadIssuesByProject(U1.settings.defaultProject)
            } catch (cause: ClientRequestException) {
                iState.value = IssueUiState.Error
            }
        }
    }

    // issue-level

    /**
     * Loads the first batch of most recent issues in the specified [project]
     * and refreshes the local caches with this new collection.
     *
     * @param withNewIssue Provide a newly created [IssueDetailed] if it has been created with a project value
     * that differs from the currently loaded one. This ensures that the newly loaded project data is retrieved
     * silently while focus stays with the new issue details screen.
     */
    fun loadIssuesByProject(project: Project, withNewIssue: IssueDetailed? = null) {
        scope.launch {
            tryLoading {
                val fullCount = client.countIssuesInProject(project.id)

                cache.setIssuesDetailed {
                    withNewIssue?.let { listOf(it) } ?: emptyList()
                }

                iState.value = if (fullCount == 0) {
                    cache.setIssuesSummary { emptyList() }

                    IssueUiState.NoResult
                } else {
                    val latestIssues = client.getIssuesByProject(project.id, ISSUE_LIST_LIMIT, 0)

                    cache.setIssuesSummary { latestIssues }

                    IssueUiState.Content(
                        issueScrollState = ListScrollPosition(0, 0),
                        issueLoadState = ListLoadState(
                            totalPageCount = fullCount.ceilDiv(),
                            currentPageIndex = if (usePagination) 1 else null,
                            canLoadMore = ISSUE_LIST_LIMIT < fullCount
                        ),
                        filterCondition = null,
                        currentIssue = withNewIssue,
                        currentIssueIndex = if (withNewIssue == null) null else 1,
                    )
                }

                mState.updateContent { oldState ->
                    oldState.copy(
                        mainScreenState = if (withNewIssue == null) {
                            MainScreenState.ISSUES_LIST
                        } else {
                            MainScreenState.ISSUE_DETAILS
                        },
                        currentProject = project,
                        issueCount = fullCount
                    )
                }
            }
        }
    }

    /**
     * Loads the next batch of most recent issues in the currently loaded project
     * and refreshes the local caches with this new collection.
     *
     * If the previous batch is the result of a filter query, the same filter query will be used for the new batch
     * with the appropriate offset.
     */
    fun loadIssuesByPage(pageIndex: Int) {
        scope.launch {
            val offset = (pageIndex - 1) * ISSUE_LIST_LIMIT
            tryLoadingWithPrevious { previousContent ->
                val projectId = mState.value.currentProject().id
                val moreIssues = iState.value.filterCondition()
                    ?.let {
                        val sortBy = mState.value.currentUser().settings.defaultSort.label.lowercase()
                        client.filterIssues(it.searchText, it.hideResolved, sortBy, projectId, ISSUE_LIST_LIMIT, offset)
                    }
                    ?: client.getIssuesByProject(projectId, ISSUE_LIST_LIMIT, offset)

                cache.setIssuesSummary { moreIssues }
                cache.setIssuesDetailed { previousContent.currentIssue?.let { listOf(it) } ?: emptyList() }

                iState.value = IssueUiState.Content(
                    issueScrollState = ListScrollPosition(0, 0),
                    issueLoadState = previousContent.issueLoadState.copy(
                        currentPageIndex = pageIndex,
                        canLoadMore = (moreIssues.size + offset) < mState.value.issueCount()
                    ),
                    filterCondition = previousContent.filterCondition,
                    currentIssue = previousContent.currentIssue,
                    currentIssueIndex = previousContent.currentIssueIndex,
                )
            }
        }
    }

    /**
     * Loads the next batch of most recent issues in the currently loaded project
     * and adds this new collection to the existing caches.
     *
     * If the previous batch is the result of a filter query, the same filter query will be used for the new batch
     * with the appropriate offset.
     */
    fun loadMoreIssues() {
        scope.launch {
            val offset = cache.getIssuesSummary().value.size
            tryLoadingWithPrevious { previousContent ->
                val projectId = mState.value.currentProject().id
                val moreIssues = iState.value.filterCondition()
                    ?.let {
                        val sortBy = mState.value.currentUser().settings.defaultSort.label.lowercase()
                        client.filterIssues(it.searchText, it.hideResolved, sortBy, projectId, ISSUE_LIST_LIMIT, offset)
                    }
                    ?: client.getIssuesByProject(projectId, ISSUE_LIST_LIMIT, offset)

                cache.setIssuesSummary { oldIssues ->
                    oldIssues + moreIssues
                }

                val lastPosition = (offset - 1).coerceAtLeast(0)
                iState.value = IssueUiState.Content(
                    issueScrollState = ListScrollPosition(lastPosition, lastPosition),
                    issueLoadState = previousContent.issueLoadState.copy(
                        currentPageIndex = null,
                        canLoadMore = (moreIssues.size + offset) < mState.value.issueCount()
                    ),
                    filterCondition = previousContent.filterCondition,
                    currentIssue = previousContent.currentIssue,
                    currentIssueIndex = previousContent.currentIssueIndex,
                )
            }
        }
    }

    /**
     * Loads the first batch of most recent issues in the specified [project] that match the specified conditions
     * and refreshes the local caches with this new collection.
     */
    fun filterIssues(project: Project, search: String, hideResolved: Boolean) {
        scope.launch {
            tryLoading {
                val parsedSearch = search.split(' ').joinToString("|")
                val filteredCount = client.countFilteredIssues(parsedSearch, hideResolved, project.id)

                cache.setIssuesDetailed { emptyList() }

                iState.value = if (filteredCount == 0) {
                    cache.setIssuesSummary { emptyList() }

                    IssueUiState.NoResult
                } else {
                    val sortBy = mState.value.currentUser().settings.defaultSort.label.lowercase()
                    val filteredIssues = client.filterIssues(parsedSearch, hideResolved, sortBy, project.id, ISSUE_LIST_LIMIT, 0)

                    cache.setIssuesSummary { filteredIssues }

                    IssueUiState.Content(
                        issueScrollState = ListScrollPosition(0, 0),
                        issueLoadState = ListLoadState(
                            totalPageCount = filteredCount.ceilDiv(),
                            currentPageIndex = if (usePagination) 1 else null,
                            canLoadMore = ISSUE_LIST_LIMIT < filteredCount
                        ),
                        filterCondition = FilterCondition(parsedSearch, hideResolved),
                        currentIssue = null,
                        currentIssueIndex = null,
                    )
                }

                mState.updateContent { oldState ->
                    oldState.copy(
                        mainScreenState = MainScreenState.FILTERED_ISSUES_LIST,
                        issueCount = filteredCount
                    )
                }
            }
        }
    }

    /**
     * Loads the top ranked issues in the current project, ordered by the specified [UserRankBy] option & grouped by `IssuePriority`,
     * and refreshes the local caches with this new collection.
     */
    fun rankIssues(option: UserRankBy) {
        scope.launch {
            tryLoading {
                cache.setIssuesDetailed { emptyList() }

                val rankedIssues = client.rankIssues(option.name.lowercase(), mState.value.currentProject().id)

                cache.setRankedIssues { rankedIssues }
                cache.setIssuesSummary { rankedIssues.flatten() }

                iState.value = IssueUiState.Content(
                    issueScrollState = ListScrollPosition(0, 0),
                    issueLoadState = ListLoadState(
                        totalPageCount = 0,
                        currentPageIndex = null,
                        canLoadMore = false
                    ),
                    filterCondition = null,
                    currentIssue = null,
                    currentIssueIndex = null,
                )

                mState.updateContent { oldState ->
                    oldState.copy(
                        issueCount = 0,
                        currentSavedRank = option,
                    )
                }
            }

        }
    }

    /**
     * Loads all unresolved issues in the currently loaded project that are within travel distance of the current user
     * and refreshes the local caches with this new collection.
     */
    fun showIssueRadar() {
        scope.launch {
            tryLoading {
                val project = mState.value.currentProject().id
                val settings = mState.value.currentUser().settings
                val filteredIssues = client.filterIssuesByDistance(project, settings.location, settings.maxTravelDistance)

                cache.setIssuesSummary { filteredIssues }
                cache.setIssuesDetailed { emptyList() }

                iState.value = if (filteredIssues.isEmpty()) {
                    IssueUiState.NoResult
                } else {
                    IssueUiState.Content(
                        issueScrollState = ListScrollPosition(0, 0),
                        issueLoadState = ListLoadState(0, null, false),
                        filterCondition = FilterCondition("distance", true),
                        currentIssue = null,
                        currentIssueIndex = null,
                    )
                }

                mState.updateContent { oldState ->
                    oldState.copy(
                        mainScreenState = MainScreenState.ISSUES_RADAR,
                        issueCount = filteredIssues.size
                    )
                }
            }
        }
    }

    /**
     * Loads all details, including associated comments, of the specified [summarized] issue.
     *
     * Checks the cache for recently loaded issues before sending a client API request.
     */
    fun showIssue(summarized: IssueSummarized) {
        scope.launch {
            val summaryIndex = cache.findIssueSummarized(summarized.issue.code)!!.index
            val pageIndexOffset = iState.value.issueLoadState().currentPageIndex?.let {
                (it - 1) * ISSUE_LIST_LIMIT
            }
            val detailedIndex = (pageIndexOffset ?: 0 ) + summaryIndex + 1

            getIssueDetailedFromCacheOrTryRequesting(summarized, detailedIndex)

            mState.updateContent { oldState ->
                oldState.copy(mainScreenState = MainScreenState.ISSUE_DETAILS)
            }
        }
    }

    fun showIssue(code: String) {
        scope.launch {
            val cachedSummary = cache.findIssueSummarized(code)
            var newIndex: Int? = null

            cachedSummary?.let { (index, _) ->
                val pageIndexOffset = iState.value.issueLoadState().currentPageIndex?.let {
                    (it - 1) * ISSUE_LIST_LIMIT
                }
                newIndex = (pageIndexOffset ?: 0 ) + index + 1
                cache.findIssueDetailed(code)?.value?.let {
                    iState.updateContent { oldState ->
                        oldState.copy(
                            currentIssue = it,
                            currentIssueIndex = newIndex,
                        )
                    }
                }
            } ?: tryRequesting {
                val (pCode, iNum) = code.split('-')
                val issueProject = cache.findProject(pCode)!!.value

                val foundIssue = client.getIssue(iNum.toLong(), issueProject.id)

                if (issueProject != mState.value.currentProject()) {
                    loadIssuesByProject(issueProject, foundIssue)
                } else {
                    cache.setIssuesDetailed { oldIssues ->
                        oldIssues + foundIssue
                    }

                    iState.updateContent { oldState ->
                        oldState.copy(
                            currentIssue = foundIssue,
                            currentIssueIndex = newIndex ?: 1,
                        )
                    }
                }
            }

            mState.updateContent { oldState ->
                oldState.copy(mainScreenState = MainScreenState.ISSUE_DETAILS)
            }
        }
    }

    /**
     * Loads all details, including associated comments, of the issue that precedes the current issue in position.
     *
     * Checks the cache for recently loaded issues before sending a client API request. If the expected issue was never
     * preloaded as part of a summarized batch, it will be retrieved & the appropriate cache updated.
     */
    fun showPreviousIssue() {
        scope.launch {
            val previousIndex = iState.value.currentIssueIndex()!! - 1
            val previousSummarized = cache.getIssuesSummary().value.getOrNull(previousIndex - 1)

            previousSummarized
                ?.let { previous ->
                    getIssueDetailedFromCacheOrTryRequesting(previous, previousIndex)
                }
                ?: tryRequesting {
                    val issue = client.getIssue(mState.value.currentProject().id, previousIndex - 1)
                    val issueSummarized = IssueSummarized(issue.issue, issue.comments.size)

                    cache.setIssuesDetailed { oldIssues ->
                        oldIssues + issue
                    }
                    cache.setIssuesSummary { oldIssues ->
                        listOf(issueSummarized) + oldIssues
                    }

                    val newPageIndex = (previousIndex - 1) / ISSUE_LIST_LIMIT + 1
                    iState.updateContent { oldState ->
                        oldState.copy(
                            issueLoadState = oldState.issueLoadState.copy(
                                currentPageIndex = newPageIndex,
                            ),
                            currentIssue = issue,
                            currentIssueIndex = previousIndex,
                        )
                    }
                }
        }
    }

    /**
     * Loads all details, including associated comments, of the issue that comes after the current issue in position.
     *
     * Checks the cache for recently loaded issues before sending a client API request. If the expected issue was never
     * preloaded as part of a summarized batch, it will be retrieved & the appropriate cache updated.
     */
    fun showNextIssue() {
        scope.launch {
            val nextIndex = iState.value.currentIssueIndex()!! + 1
            val nextSummarized = cache.getIssuesSummary().value.getOrNull(nextIndex - 1)

            nextSummarized
                ?.let { next ->
                    getIssueDetailedFromCacheOrTryRequesting(next, nextIndex)
                }
                ?: tryRequesting { ->
                    val issue = client.getIssue(mState.value.currentProject().id, nextIndex - 1)
                    val issueSummarized = IssueSummarized(issue.issue, issue.comments.size)

                    cache.setIssuesDetailed { oldIssues ->
                        oldIssues + issue
                    }
                    cache.setIssuesSummary { oldIssues ->
                        oldIssues + issueSummarized
                    }

                    val newPageIndex = (nextIndex - 1) / ISSUE_LIST_LIMIT + 1
                    iState.updateContent { oldState ->
                        oldState.copy(
                            issueLoadState = oldState.issueLoadState.copy(
                                currentPageIndex = newPageIndex,
                                canLoadMore = (newPageIndex * ISSUE_LIST_LIMIT) < mState.value.issueCount()
                            ),
                            currentIssue = issue,
                            currentIssueIndex = nextIndex,
                        )
                    }
                }
        }
    }

    private suspend fun getIssueDetailedFromCacheOrTryRequesting(summarized: IssueSummarized, index: Int) {
        val cachedIssue = cache.findIssueDetailed(summarized.issue.code)?.value

        cachedIssue?.let {
            iState.updateContent { oldState ->
                oldState.copy(
                    currentIssue = it,
                    currentIssueIndex = index,
                )
            }
        } ?: tryRequesting {
            val issue = client.getIssue(summarized.issue.number, summarized.issue.projectId)

            cache.setIssuesDetailed { oldIssues ->
                oldIssues + issue
            }

            iState.updateContent { oldState ->
                oldState.copy(
                    currentIssue = issue,
                    currentIssueIndex = index,
                )
            }
        }
    }

    /**
     * Stores a newly created [issue], retrieves any server-side generated values, & updates the appropriate caches.
     *
     * If the new issue has been created with a project value that differs from the currently loaded one,
     * the first batch of most recent issues in the new project will also be loaded & the caches refreshed.
     */
    fun createIssue(issue: Issue) {
        scope.launch {
            tryRequesting {
                val newIssue = client.addIssue(issue)

                val newIssueProject = cache.findProject(newIssue.projectId)!!.value
                val newSummarized = IssueSummarized(newIssue, 0)
                val newDetailed = IssueDetailed(newIssue, emptyList())

                if (newIssueProject != mState.value.currentProject()) {
                    loadIssuesByProject(newIssueProject, newDetailed)
                } else {
                    cache.setIssuesSummary { oldIssues ->
                        listOf(newSummarized) + oldIssues
                    }
                    cache.setIssuesDetailed { oldIssues ->
                        oldIssues + newDetailed
                    }

                    iState.updateContent { oldState ->
                        oldState.copy(
                            currentIssue = newDetailed,
                            currentIssueIndex = 1,
                        )
                    }

                    mState.updateContent { oldState ->
                        oldState.copy(
                            mainScreenState = MainScreenState.ISSUE_DETAILS,
                            issueCount = oldState.issueCount + 1
                        )
                    }
                }
            }
        }
    }

    /**
     * Updates the specified [issue] & retrieves any server-side generated values.
     *
     * The previous version of the issue will also be replaced by the updated version in appropriate caches.
     */
    fun updateIssue(issue: IssueDetailed) {
        scope.launch {
            tryRequesting {
                val updatedWatchers = issue.issue.watchers
                    .toMutableSet()
                    .plus(mState.value.currentUser().takeIf { it.settings.starOnIssueUpdate }?.id)
                    .filterNotNull()
                val updated = client.editIssue(issue.issue.copy(watchers = updatedWatchers))

                val oldDetailedIndex = cache.findIssueDetailed(issue.issue.code)?.index!!
                val updatedDetailed = issue.copy(issue = updated)

                cache.setIssuesDetailed { oldIssues ->
                    oldIssues.toMutableList().apply {
                        removeAt(oldDetailedIndex)
                        add(updatedDetailed)
                    }
                }

                cache.findIssueSummarized(issue.issue.code)?.let { (oldSummarizedIndex, oldSummarized) ->
                    val updatedSummarized = oldSummarized.copy(issue = updated)

                    cache.setIssuesSummary { oldIssues ->
                        oldIssues.toMutableList().apply {
                            removeAt(oldSummarizedIndex)
                            add(0, updatedSummarized)
                        }
                    }

                    if (updated.state == IssueState.COMPLETED && oldSummarized.issue.state != IssueState.COMPLETED) {
                        addNotifications(NotificationType.ClosedIssue, updated)
                    } else {
                        addNotifications(NotificationType.UpdatedIssue, updated)
                    }
                }

                cache.findRankedIssue(issue.issue.code)?.let { (oldOuterRankedIndex, oldRankedIssue) ->
                    val updatedRanked = oldRankedIssue.value.copy(issue = updated)

                    cache.setRankedIssues { oldIssues ->
                        oldIssues.toMutableList().apply {
                            val oldOuterList = get(oldOuterRankedIndex).toMutableList().apply {
                                removeAt(oldRankedIssue.index)
                                add(0, updatedRanked)
                            }
                            set(oldOuterRankedIndex, oldOuterList)
                        }
                    }
                }

                iState.updateContent { oldState ->
                    oldState.copy(
                        currentIssue = updatedDetailed,
                        currentIssueIndex = 1,
                    )
                }
            }
        }
    }

    /**
     * Updates the specified [issue] & retrieves any server-side generated values.
     *
     * The previous version of the issue will also be replaced by the updated version in appropriate caches,
     * but in such a way that recomposition is only triggered for the immediate composable. From the perspective
     * of parent composables, like the lazy list of summarized issues, this update will appear silent.
     */
    fun updateIssue(userId: Int, toggle: String, issue: Issue) {
        scope.launch {
            tryRequesting {
                val updated = client.editIssue(issue, userId, toggle)

                cache.findIssueSummarized(issue.code)?.let { (oldIndex, oldSummarized) ->
                    val updatedSummarized = oldSummarized.copy(issue = updated)
                    cache.setIssuesSummary { oldIssues ->
                        oldIssues.toMutableList().apply {
                            set(oldIndex, updatedSummarized)
                        }
                    }
                }
                cache.findRankedIssue(issue.code)?.let { (oldOuterRankedIndex, oldRankedIssue) ->
                    val updatedRanked = oldRankedIssue.value.copy(issue = updated)
                    cache.setRankedIssues { oldIssues ->
                        oldIssues.toMutableList().apply {
                            val oldOuterList = get(oldOuterRankedIndex).toMutableList().apply {
                                set(oldRankedIssue.index, updatedRanked)
                            }
                            set(oldOuterRankedIndex, oldOuterList)
                        }
                    }
                }
                val updatedDetailed = cache.findIssueDetailed(issue.code)?.let { (oldIndex, oldDetailed) ->
                    val temp = oldDetailed.copy(issue = updated)
                    cache.setIssuesDetailed { oldIssues ->
                        oldIssues.toMutableList().apply {
                            removeAt(oldIndex)
                            add(temp)
                        }
                    }
                    temp
                }

                iState.updateContent { oldState ->
                    oldState.copy(
                        currentIssue = if (oldState.currentIssue == null) null else updatedDetailed,
                    )
                }
            }
        }
    }

    /**
     * Deletes the specified [issue] & clears it from the appropriate caches.
     *
     * The state will reset to the first item in the list of loaded issues.
     */
    fun removeIssue(issue: IssueDetailed) {
        scope.launch {
            tryRequesting {
                client.deleteIssue(issue.issue.number, issue.issue.projectId)

                cache.findIssueSummarized(issue.issue.code)?.index?.let {oldSummarizedIndex ->
                    cache.setIssuesSummary { oldIssues ->
                        oldIssues.toMutableList().apply { removeAt(oldSummarizedIndex) }
                    }
                }
                cache.findRankedIssue(issue.issue.code)?.let { (oldOuterRankedIndex, oldRankedIssue) ->
                    cache.setRankedIssues { oldIssues ->
                        oldIssues.toMutableList().apply {
                            val oldOuterList = get(oldOuterRankedIndex).toMutableList().apply {
                                removeAt(oldRankedIssue.index)
                            }
                            set(oldOuterRankedIndex, oldOuterList)
                        }
                    }
                }

                val oldDetailedIndex = cache.findIssueDetailed(issue.issue.code)?.index!!

                cache.setIssuesDetailed { oldIssues ->
                    oldIssues.toMutableList().apply { removeAt(oldDetailedIndex) }
                }

                iState.updateContent { oldState ->
                    oldState.copy(
                        issueLoadState = oldState.issueLoadState.copy(currentPageIndex = 1),
                        currentIssue = null,
                        currentIssueIndex = null,
                    )
                }

                mState.updateContent { oldState ->
                    oldState.copy(
                        mainScreenState = MainScreenState.ISSUES_LIST,
                        issueCount = oldState.issueCount - 1
                    )
                }
            }
        }
    }

    // user-level

    /** Switches to the screen showing user data settings. */
    fun showUserAccount() {
        mState.updateContent { oldState ->
            oldState.copy(
                mainScreenState = MainScreenState.USER_ACCOUNT,
                issueCount = 0,
            )
        }

        iState.value = IssueUiState.Content(
            issueScrollState = ListScrollPosition(0, 0),
            issueLoadState = ListLoadState(
                totalPageCount = 0,
                currentPageIndex = null,
                canLoadMore = false
            ),
            filterCondition = null,
            currentIssue = null,
            currentIssueIndex = null,
        )
    }

    /** Updates the current user with the new specified [settings]. */
    fun editUserSettings(settings: UserSettings) {
        scope.launch {
            tryRequesting {
                val updated = mState.value.currentUser().copy(settings = settings)
                client.editUser(updated)

                val oldIndex = cache.findUser(updated.id)!!.index
                cache.setUsers { oldUsers ->
                    oldUsers.toMutableList().apply {
                        set(oldIndex, updated)
                    }
                }

                mState.updateContent { oldState ->
                    oldState.copy(currentUser = updated)
                }
            }
        }
    }

    // comment-level

    /** Stores a newly created [comment], retrieves any server-side generated values, & updates the appropriate caches. */
    fun addComment(comment: Comment) {
        scope.launch {
            tryRequesting {
                val newComment = client.addComment(comment)

                val oldDetailed = iState.value.currentIssue()!!
                val oldDetailedIndex = cache.findIssueDetailed(oldDetailed.issue.code)!!.index
                val updatedDetailed = oldDetailed.copy(
                    comments = oldDetailed.comments + newComment,
                    issue = oldDetailed.issue.copy(modifiedAt = newComment.modifiedAt)
                )

                cache.setIssuesDetailed { oldIssues ->
                    oldIssues.toMutableList().apply {
                        removeAt(oldDetailedIndex)
                        add(updatedDetailed)
                    }
                }

                cache.findIssueSummarized(oldDetailed.issue.code)?.let { (oldSummarizedIndex, oldSummarized) ->
                    val updatedSummarized = oldSummarized.copy(
                        commentCount = oldSummarized.commentCount + 1,
                        issue = oldSummarized.issue.copy(modifiedAt = newComment.modifiedAt)
                    )

                    cache.setIssuesSummary { oldIssues ->
                        oldIssues.toMutableList().apply {
                            removeAt(oldSummarizedIndex)
                            add(0, updatedSummarized)
                        }
                    }

                    addNotifications(NotificationType.CommentedOnIssue, updatedSummarized.issue)
                }

                cache.findRankedIssue(oldDetailed.issue.code)?.let { (oldOuterRankedIndex, oldRankedIssue) ->
                    val updatedRanked = oldRankedIssue.value.copy(
                        commentCount = oldRankedIssue.value.commentCount + 1,
                        issue = oldRankedIssue.value.issue.copy(modifiedAt = newComment.modifiedAt)
                    )

                    cache.setRankedIssues { oldIssues ->
                        oldIssues.toMutableList().apply {
                            val oldOuterList = get(oldOuterRankedIndex).toMutableList().apply {
                                removeAt(oldRankedIssue.index)
                                add(0, updatedRanked)
                            }
                            set(oldOuterRankedIndex, oldOuterList)
                        }
                    }
                }

                addMentions(NotificationType.Mentioned, newComment)

                iState.updateContent { oldState ->
                    oldState.copy(
                        currentIssue = updatedDetailed,
                        currentIssueIndex = 1,
                    )
                }
            }
        }
    }

    /**
     * Updates the specified [comment] & retrieves any server-side generated values.
     *
     * The previous version of the comment will also be replaced by the updated version in appropriate caches,
     * but in such a way that recomposition is only triggered for the immediate composable. From the perspective
     * of parent composables, like the lazy list of summarized issues, this update will appear silent.
     */
    fun editComment(comment: Comment) {
        scope.launch {
            tryRequesting {
                val updatedComment = client.editComment(comment)

                val oldDetailed = iState.value.currentIssue()!!
                val oldDetailedIndex = cache.findIssueDetailed(oldDetailed.issue.code)?.index!!
                val oldComments = oldDetailed.comments
                val oldCommentIndex = oldComments.indexOfFirst { it.id == updatedComment.id }
                val updatedComments = oldComments.toMutableList().apply { set(oldCommentIndex, updatedComment) }
                val updatedDetailed = oldDetailed.copy(comments = updatedComments)

                cache.setIssuesDetailed { oldIssues ->
                    oldIssues.toMutableList().apply {
                        removeAt(oldDetailedIndex)
                        add(updatedDetailed)
                    }
                }

                iState.updateContent { oldState ->
                    oldState.copy(currentIssue = updatedDetailed)
                }
            }
        }
    }

    /**
     * Deletes the specified [comment] & clears it from the appropriate caches,
     * so the cached parent issue will also refresh accordingly.
     */
    fun deleteComment(comment: Comment) {
        scope.launch {
            tryRequesting {
                client.deleteComment(comment.id, comment.issueNumber, comment.projectId)

                val oldDetailed = iState.value.currentIssue()!!
                val oldDetailedIndex = cache.findIssueDetailed(oldDetailed.issue.code)!!.index
                val updatedDetailed = oldDetailed.copy(comments = oldDetailed.comments - comment)

                cache.setIssuesDetailed { oldIssues ->
                    listOf(updatedDetailed) + (oldIssues - oldDetailed)
                    oldIssues.toMutableList().apply {
                        removeAt(oldDetailedIndex)
                        add(updatedDetailed)
                    }
                }

                cache.findIssueSummarized(oldDetailed.issue.code)?.let { (oldSummarizedIndex, oldSummarized) ->
                    val updatedSummarized = oldSummarized.copy(commentCount = oldSummarized.commentCount - 1)

                    cache.setIssuesSummary { oldIssues ->
                        oldIssues.toMutableList().apply {
                            removeAt(oldSummarizedIndex)
                            add(0, updatedSummarized)
                        }
                    }
                }

                cache.findRankedIssue(oldDetailed.issue.code)?.let { (oldOuterRankedIndex, oldRankedIssue) ->
                    val updatedRanked = oldRankedIssue.value.copy(commentCount = oldRankedIssue.value.commentCount - 1)

                    cache.setRankedIssues { oldIssues ->
                        oldIssues.toMutableList().apply {
                            val oldOuterList = get(oldOuterRankedIndex).toMutableList().apply {
                                removeAt(oldRankedIssue.index)
                                add(0, updatedRanked)
                            }
                            set(oldOuterRankedIndex, oldOuterList)
                        }
                    }
                }

                iState.updateContent { oldState ->
                    oldState.copy(
                        currentIssue = updatedDetailed,
                        currentIssueIndex = 1
                    )
                }
            }
        }
    }

    // notification-level

    private fun addNotifications(type: NotificationType, issue: Issue) {
        scope.launch {
            tryRequesting {
                val sender = mState.value.currentUser()
                val toNotify = issue.watchers.filter { it != sender.id || sender.settings.notifyOnSelfChanges }
                val notifications = toNotify.map {
                    Notification(
                        id = DummyValues.IDL,
                        notificationTypeId = type.id,
                        receiverId = it,
                        senderId = sender.id,
                        issueNumber = issue.number,
                        projectId = issue.projectId,
                        createdAt = DummyValues.TS
                    )
                }

                if (notifications.isEmpty()) return@tryRequesting

                val newNotifications = client.addNotifications(notifications)

                newNotifications.find { it.senderId == it.receiverId }?.let {
                    val newSessionNotification = SessionNotification(
                        id = it.id,
                        receiverId = it.receiverId,
                        message = type.message,
                        senderName = sender.fullName,
                        senderAvatar = sender.settings.avatarIcon,
                        senderAvatarTint = sender.settings.avatarTint,
                        issueCode = issue.code,
                        createdAt = it.createdAt,
                        isRead = false,
                    )

                    cache.setNotifications { oldNotifications ->
                        oldNotifications.toMutableList().apply {
                            listOf(newSessionNotification) + oldNotifications
                        }
                    }

                    mState.updateContent { oldState ->
                        oldState.copy(hasUnreadNotifications = true)
                    }
                }
            }
        }
    }

    private fun addMentions(type: NotificationType, comment: Comment) {
        scope.launch {
            tryRequesting {
                val sender = mState.value.currentUser()
                val toNotify = comment.getNewMentions().mapNotNull { username ->
                    cache.findUser(username)?.value?.id
                }
                val notifications = toNotify.map {
                    Notification(
                        id = DummyValues.IDL,
                        notificationTypeId = type.id,
                        receiverId = it,
                        senderId = sender.id,
                        issueNumber = comment.issueNumber,
                        projectId = comment.projectId,
                        createdAt = DummyValues.TS
                    )
                }

                if (notifications.isEmpty()) return@tryRequesting

                client.addNotifications(notifications)
            }
        }
    }

    fun updateNotification(notification: SessionNotification) {
        scope.launch {
            tryRequesting {
                val updated = notification.copy(isRead = notification.isRead)
                client.editNotification(updated)

                cache.findNotification(notification.id)?.index?.let { oldIndex ->
                    cache.setNotifications { oldNotifications ->
                        oldNotifications.toMutableList().apply {
                            set(oldIndex, updated)
                        }
                    }
                }

                mState.updateContent { oldState ->
                    oldState.copy(hasUnreadNotifications = cache.getNotifications().value.any { !it.isRead })
                }
            }
        }
    }

    // miscellaneous

    fun handleBackButton() {
        when (mState.value.mainScreenState()) {
            MainScreenState.ISSUE_DETAILS -> {
                var loadedFromFilter = false

                iState.updateContent { oldState ->
                    loadedFromFilter = oldState.filterCondition != null
                    oldState.copy(
                        currentIssue = null,
                        currentIssueIndex = null,
                    )
                }

                if (!loadedFromFilter && usePagination) {
                    iState.value.issueLoadState().currentPageIndex?.let {
                        loadIssuesByPage(it)
                    }
                        ?: loadIssuesByProject(mState.value.currentProject())
                }

                mState.updateContent { oldState ->
                    oldState.copy(
                        mainScreenState = if (loadedFromFilter) MainScreenState.FILTERED_ISSUES_LIST else MainScreenState.ISSUES_LIST
                    )
                }
            }

            MainScreenState.ISSUES_RADAR,
            MainScreenState.FILTERED_ISSUES_LIST,
            MainScreenState.USER_ACCOUNT -> {
                // refresh cache with all (batched) non-filtered issues
                loadIssuesByProject(mState.value.currentProject())
            }

            MainScreenState.ISSUES_LIST -> {} // should never be reached, as disabled
        }
    }

    fun rememberListPosition(newPosition: ListScrollPosition) {
        iState.updateContent { oldState ->
            oldState.copy(issueScrollState = newPosition)
        }
    }

    fun cleanUp() {
        scope.launch {
            client.cleanUp()
        }
    }

    private suspend fun tryRequesting(body: suspend () -> Unit) {
        try {
            body()
        } catch (cause: ClientRequestException) {
            iState.value = IssueUiState.Error
        }
    }

    private suspend fun tryLoading(body: suspend () -> Unit) {
        iState.value = IssueUiState.Loading
        tryRequesting(body)
    }

    private suspend fun tryLoadingWithPrevious(body: suspend (IssueUiState.Content) -> Unit) {
        val previousContent = iState.value as? IssueUiState.Content
            ?: error("Pre-load state holds no content: $iState")

        iState.value = IssueUiState.Loading
        try {
            body(previousContent)
        } catch (cause: ClientRequestException) {
            iState.value = IssueUiState.Error
        }
    }

    companion object {
        private const val ISSUE_LIST_LIMIT = 10

        private fun Int.ceilDiv(
            limit: Int = ISSUE_LIST_LIMIT
        ): Int = this.floorDiv(limit) + this.rem(limit).sign.absoluteValue

        private val P1 = Project(
            id = 1,
            name = "Ghost",
            code = "GHST"
        )

        private val U1 = User(
            id = 1,
            fullName = "Damien Colt",
            username = "damien.colt",
            settings = UserSettings(
                avatarIcon = 5,
                avatarTint = 5,
                defaultProject= P1,
                location = Location(38.5829, -121.4747),
                maxTravelDistance = 5000,
                dateFormat = UserDateFormat.DAY_MONTH_YEAR_TIME_NAMED.pattern,
                defaultSort = UserSort.UPDATED,
                notifyOnSelfChanges = false,
                notifyOnMention = true,
                unstarOnIssueClose = false,
                starOnIssueCreate = true,
                starOnIssueUpdate = false,
                starOnIssueAssigned = true,
                starOnIssueUpvote = false,
            )
        )
    }
}

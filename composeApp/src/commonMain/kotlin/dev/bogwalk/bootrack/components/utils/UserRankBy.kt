package dev.bogwalk.bootrack.components.utils

import androidx.compose.runtime.Composable
import bootrack.composeapp.generated.resources.Res
import bootrack.composeapp.generated.resources.rank_by_open
import bootrack.composeapp.generated.resources.rank_by_stars
import bootrack.composeapp.generated.resources.rank_by_votes
import org.jetbrains.compose.resources.stringResource

enum class UserRankBy {
    OPEN,
    STARS,
    UPVOTES
}

@Composable
internal fun UserRankBy.getLabel(): String = when (this) {
    UserRankBy.OPEN -> stringResource(Res.string.rank_by_open)
    UserRankBy.STARS -> stringResource(Res.string.rank_by_stars)
    UserRankBy.UPVOTES -> stringResource(Res.string.rank_by_votes)
}

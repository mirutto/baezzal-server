package server.follow.application

data class FollowResult(
    val followerId: Long,
    val followeeId: Long,
)

data class MyFollowStats(
    val followerCount: Long,
    val followeeCount: Long,
)

data class MemberFollowSummaryResult(
    val followerCount: Long,
    val isFollowing: Boolean,
)

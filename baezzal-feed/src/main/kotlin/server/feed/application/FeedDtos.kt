package server.feed.application

import server.feed.domain.FeedThumbnailStatus

data class FeedPostData(
    val postId: Long,
    val thumbnailImageUrl: String,
    val imageAspectRatio: Double,
)

data class FeedTeamSummaryData(
    val teamId: Long,
    val name: String,
    val postCount: Long,
    val thumbnailUrls: List<String>,
)

data class FeedPostDetailData(
    val postId: Long,
    val viewCount: Long,
    val rawImageUrl: String,
    val publicImageUrl: String,
    val imageAspectRatio: Double,
    val status: String,
    val author: FeedAuthorData,
    val description: String,
    val tagTitles: List<String>,
    val collectionPostCount: Long,
)

data class FeedAuthorData(
    val memberId: Long,
    val nickname: String,
    val username: String,
    val profileImage: String,
    val preferredTeam: FeedTeamData?,
)

data class FeedTeamData(
    val teamId: Long,
    val name: String,
)

data class FeedPostRowData(
    val postId: Long,
    val thumbnailImageUrl: String,
    val imageAspectRatio: Double,
) {
    fun toFeedPostData(): FeedPostData =
        FeedPostData(
            postId = postId,
            thumbnailImageUrl = thumbnailImageUrl,
            imageAspectRatio = imageAspectRatio,
        )
}

data class FeedPostDetailRowData(
    val postId: Long,
    val memberId: Long,
    val viewCount: Long,
    val rawImageUrl: String,
    val publicImageUrl: String,
    val imageAspectRatio: Double,
    val status: FeedThumbnailStatus,
    val description: String,
)

data class FeedMemberRowData(
    val memberId: Long,
    val nickname: String,
    val username: String,
    val profileImage: String,
    val preferredTeamId: Long?,
)

data class FeedTeamSummaryRowData(
    val teamId: Long,
    val name: String,
)

data class FeedTeamPostCountRowData(
    val teamId: Long,
    val postCount: Long,
)

data class FeedTeamThumbnailRowData(
    val teamId: Long,
    val thumbnailUrl: String,
)

package server.feed.application

import server.feed.domain.FeedThumbnailStatus
import java.time.LocalDateTime

data class FeedPostData(
    val postId: Long,
    val thumbnailImageUrl: String,
    val publicImageUrl: String,
    val imageAspectRatio: Double,
)

data class FeedTeamSummaryData(
    val teamCode: String,
    val name: String,
    val postCount: Long,
    val thumbnailUrls: List<String>,
)

data class FeedCollectionData(
    val collectionId: Long,
    val name: String,
    val postCount: Long,
    val lastPostRuleModifiedAt: LocalDateTime,
    val thumbnailUrl: String,
    val isPublic: Boolean,
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
    val teamCode: String,
    val name: String,
)

data class FeedPostRowData(
    val postId: Long,
    val thumbnailImageUrl: String,
    val publicImageUrl: String,
    val imageAspectRatio: Double,
) {
    fun toFeedPostData(): FeedPostData =
        FeedPostData(
            postId = postId,
            thumbnailImageUrl = thumbnailImageUrl,
            publicImageUrl = publicImageUrl,
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
    val teamCode: String,
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

data class FeedCollectionRowData(
    val collectionId: Long,
    val name: String,
    val lastPostRuleModifiedAt: LocalDateTime,
    val thumbnailUrl: String,
    val isPublic: Boolean,
)

data class FeedCollectionPostCountRowData(
    val collectionId: Long,
    val postCount: Long,
)

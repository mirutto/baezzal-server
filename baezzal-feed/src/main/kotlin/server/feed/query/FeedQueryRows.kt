package server.feed.query

import server.feed.model.post.FeedThumbnailStatus
import java.time.LocalDateTime

data class FeedPostDetailQueryRow(
    val postId: Long,
    val memberId: Long,
    val rawImageUrl: String,
    val publicImageUrl: String,
    val imageAspectRatio: Double,
    val status: FeedThumbnailStatus,
    val description: String,
)

data class FeedAuthorQueryRow(
    val memberId: Long,
    val nickname: String,
    val username: String,
    val thumbnailProfileImage: String,
    val preferredTeamId: Long?,
)

data class FeedTeamSummaryQueryRow(
    val teamId: Long,
    val teamCode: String,
    val name: String,
)

data class FeedTeamPostCountQueryRow(
    val teamId: Long,
    val postCount: Long,
)

data class FeedTeamThumbnailQueryRow(
    val teamId: Long,
    val thumbnailUrl: String,
)

data class FeedCollectionQueryRow(
    val collectionId: Long,
    val name: String,
    val lastPostRuleModifiedAt: LocalDateTime,
    val thumbnailUrl: String,
    val isPublic: Boolean,
)

data class FeedCollectionPostCountQueryRow(
    val collectionId: Long,
    val postCount: Long,
)

data class DailyPopularTagQueryRow(
    val tagId: Long,
    val title: String,
    val searchCount: Long,
)

data class DailyPopularPostQueryRow(
    val postId: Long,
    val thumbnailImageUrl: String,
    val publicImageUrl: String,
    val imageAspectRatio: Double,
    val score: Long,
    val createdAt: LocalDateTime,
)

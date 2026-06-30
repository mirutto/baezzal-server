package server.feed.application

import java.time.LocalDateTime
import java.util.Base64

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

data class DailyPopularTagData(
    val rank: Int,
    val tagId: Long,
    val title: String,
    val searchCount: Long,
)

data class TagAutocompleteData(
    val tagId: Long,
    val title: String,
)

data class DailyPopularPostSliceResult(
    val posts: List<FeedPostData>,
    val hasNext: Boolean,
    val nextCursor: String?,
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
    val thumbnailProfileImage: String,
    val preferredTeam: FeedTeamData?,
)

data class FeedTeamData(
    val teamCode: String,
    val name: String,
)

data class DailyPopularPostCursor(
    val score: Long,
    val createdAt: LocalDateTime,
    val postId: Long,
) {
    fun encode(): String = Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString("$score|$createdAt|$postId".toByteArray())

    companion object {
        fun decode(cursor: String): DailyPopularPostCursor {
            val decoded = String(Base64.getUrlDecoder().decode(cursor))
            val (score, createdAt, postId) = decoded.split("|", limit = 3)
            return DailyPopularPostCursor(
                score = score.toLong(),
                createdAt = LocalDateTime.parse(createdAt),
                postId = postId.toLong(),
            )
        }
    }
}

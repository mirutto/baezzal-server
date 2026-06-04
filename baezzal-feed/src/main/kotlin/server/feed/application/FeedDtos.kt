package server.feed.application

data class FeedPostData(
    val postId: Long,
    val viewCount: Long,
    val image: FeedImageData,
    val thumbnail: FeedImageData,
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
    val image: FeedImageData,
    val thumbnail: FeedImageData,
    val author: FeedAuthorData,
    val description: String,
    val tagTitles: List<String>,
    val collectionPostCount: Long,
)

data class FeedAuthorData(
    val memberId: Long,
    val nickname: String,
    val profileImage: String,
    val preferredTeam: FeedTeamData?,
)

data class FeedTeamData(
    val teamId: Long,
    val name: String,
)

data class FeedImageData(
    val url: String,
    val width: Int?,
    val height: Int?,
    val aspectRatio: Double?,
)

data class FeedPostRowData(
    val postId: Long,
    val viewCount: Long,
    val imageUrl: String,
    val imageWidth: Int?,
    val imageHeight: Int?,
    val imageAspectRatio: Double?,
    val thumbnailUrl: String,
    val thumbnailWidth: Int?,
    val thumbnailHeight: Int?,
    val thumbnailAspectRatio: Double?,
) {
    fun toFeedPostData(): FeedPostData =
        FeedPostData(
            postId = postId,
            viewCount = viewCount,
            image = FeedImageData(
                url = imageUrl,
                width = imageWidth,
                height = imageHeight,
                aspectRatio = imageAspectRatio,
            ),
            thumbnail = FeedImageData(
                url = thumbnailUrl,
                width = thumbnailWidth,
                height = thumbnailHeight,
                aspectRatio = thumbnailAspectRatio,
            ),
        )
}

data class FeedPostDetailRowData(
    val postId: Long,
    val memberId: Long,
    val viewCount: Long,
    val imageUrl: String,
    val imageWidth: Int?,
    val imageHeight: Int?,
    val imageAspectRatio: Double?,
    val thumbnailUrl: String,
    val thumbnailWidth: Int?,
    val thumbnailHeight: Int?,
    val thumbnailAspectRatio: Double?,
    val description: String,
)

data class FeedMemberRowData(
    val memberId: Long,
    val nickname: String,
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

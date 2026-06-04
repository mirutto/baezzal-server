package server.post.application

import java.time.LocalDateTime

data class PostCreatedEvent(
    val postId: Long,
    val imageUrl: String,
)

data class PostViewedEvent(
    val userId: Long,
    val postId: Long,
    val viewedAt: LocalDateTime,
)

data class ImageAssetEvent(
    val url: String,
    val width: Int?,
    val height: Int?,
    val aspectRatio: Double?,
)

data class ThumbnailUpdatedEvent(
    val postId: Long,
    val originalImage: ImageAssetEvent,
    val thumbnailImage: ImageAssetEvent,
)

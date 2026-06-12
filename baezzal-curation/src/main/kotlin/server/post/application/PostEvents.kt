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

data class PostImageProcessedEvent(
    val postId: Long,
    val rawImageUrl: String,
    val publicImageUrl: String,
    val thumbnailImageUrl: String,
    val aspectRatio: Double,
)

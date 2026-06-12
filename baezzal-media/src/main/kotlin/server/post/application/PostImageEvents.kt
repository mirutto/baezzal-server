package server.post.application

data class PostCreatedEvent(
    val postId: Long,
    val imageUrl: String,
)

data class PostImageProcessedEvent(
    val postId: Long,
    val rawImageUrl: String,
    val publicImageUrl: String,
    val thumbnailImageUrl: String,
    val aspectRatio: Double,
)

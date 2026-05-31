package server.thumbnail.domain

data class PostCreatedEvent(
    val postId: Long,
    val imageUrl: String,
)

data class ThumbnailUpdatedEvent(
    val postId: Long,
    val thumbnailUrl: String,
)

package server.post.application

data class PostCreatedEvent(
    val postId: Long,
    val imageUrl: String,
)

data class MediaUploadUrlIssuedEvent(
    val prefix: String,
    val objectKey: String,
    val fileUrl: String,
    val expiresInSeconds: Int,
)

data class ThumbnailUpdatedEvent(
    val postId: Long,
    val thumbnailUrl: String,
)

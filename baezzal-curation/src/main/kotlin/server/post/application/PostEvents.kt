package server.post.application

data class PostCreatedEvent(
    val postId: Long,
    val imageUrl: String,
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

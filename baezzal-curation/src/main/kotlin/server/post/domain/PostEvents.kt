package server.post.domain

data class PostCreatedEvent(
    val postId: Long,
    val imageUrl: String,
)

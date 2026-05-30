package server.post.implementation

data class PostCreatedEvent(
    val postId: Long,
    val imageUrl: String,
)

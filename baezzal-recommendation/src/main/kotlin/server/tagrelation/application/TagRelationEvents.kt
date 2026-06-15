package server.tagrelation.application

data class PostCreatedEvent(
    val postId: Long,
    val imageUrl: String,
)

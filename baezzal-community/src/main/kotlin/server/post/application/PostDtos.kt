package server.post.application

import server.post.domain.Post

data class CreatePostCommand(
    val imageUrl: String,
    val imageAspectRatio: Double,
    val description: String = "",
    val teamId: Long? = null,
    val tagTitles: List<String> = emptyList(),
)

data class PostIdResult(
    val postId: Long,
) {
    constructor(post: Post) : this(postId = post.id)
}

data class PostBatchResult(
    val postCount: Int,
    val viewCount: Long,
)

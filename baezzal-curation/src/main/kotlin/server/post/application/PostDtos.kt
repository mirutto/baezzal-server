package server.post.application

import server.post.domain.Post
import server.tag.domain.Tag

data class CreatePostCommand(
    val imageUrl: String,
    val description: String = "",
    val teamId: Long? = null,
    val tagTitles: List<String> = emptyList(),
)

data class CreatePostResult(
    val postId: Long,
    val memberId: Long,
    val imageUrl: String,
    val thumbnailUrl: String,
    val thumbnailStatus: String,
    val description: String,
    val teamId: Long?,
    val tagTitles: List<String>,
) {
    constructor(
        post: Post,
        tags: List<Tag>,
    ) : this(
        postId = post.id,
        memberId = post.memberId,
        imageUrl = post.imageUrl,
        thumbnailUrl = post.thumbnailUrl,
        thumbnailStatus = post.thumbnailStatus.name,
        description = post.description,
        teamId = post.teamId,
        tagTitles = tags.map(Tag::title),
    )
}

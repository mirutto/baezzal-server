package server.post.application

import global.image.ImageVersionsData
import server.post.domain.Post
import server.tag.domain.Tag

data class CreatePostCommand(
    val imageUrl: String,
    val imageAspectRatio: Double,
    val description: String = "",
    val teamId: Long? = null,
    val tagTitles: List<String> = emptyList(),
)

data class PostData(
    val postId: Long,
    val memberId: Long,
    val viewCount: Long,
    val imageUrl: String,
    val thumbnailUrl: String,
    val image: ImageVersionsData,
    val description: String,
    val teamId: Long?,
    val tagTitles: List<String>,
) {
    constructor(
        post: Post,
        tagTitles: List<String>,
    ) : this(
        postId = post.id,
        memberId = post.memberId,
        viewCount = post.viewCount,
        imageUrl = post.imageUrl,
        thumbnailUrl = post.thumbnailUrl,
        image = ImageVersionsData(post.image),
        description = post.description,
        teamId = post.teamId,
        tagTitles = tagTitles,
    )
}

data class CreatePostResult(
    val postId: Long,
    val memberId: Long,
    val viewCount: Long,
    val imageUrl: String,
    val thumbnailUrl: String,
    val image: ImageVersionsData,
    val description: String,
    val teamId: Long?,
    val tagTitles: List<String>,
) {
    constructor(
        post: Post,
        tags: List<Tag>,
    ) : this(PostData(post, tags.map(Tag::title)))

    constructor(post: PostData) : this(
        postId = post.postId,
        memberId = post.memberId,
        viewCount = post.viewCount,
        imageUrl = post.imageUrl,
        thumbnailUrl = post.thumbnailUrl,
        image = post.image,
        description = post.description,
        teamId = post.teamId,
        tagTitles = post.tagTitles,
    )
}

data class PostBatchResult(
    val postCount: Int,
    val viewCount: Long,
)

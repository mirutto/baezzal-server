package server.post.application

import server.post.domain.ImageAsset
import server.post.domain.Post
import server.tag.domain.Tag

data class CreatePostCommand(
    val imageUrl: String,
    val imageWidth: Int,
    val imageHeight: Int,
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
    val originalImage: ImageAssetData,
    val thumbnailUrl: String,
    val thumbnailImage: ImageAssetData,
    val thumbnailStatus: String,
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
        imageUrl = post.originalImage.url,
        originalImage = ImageAssetData(post.originalImage),
        thumbnailUrl = post.thumbnailImage.url,
        thumbnailImage = ImageAssetData(post.thumbnailImage),
        thumbnailStatus = post.thumbnailStatus.name,
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
    val originalImage: ImageAssetData,
    val thumbnailUrl: String,
    val thumbnailImage: ImageAssetData,
    val thumbnailStatus: String,
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
        originalImage = post.originalImage,
        thumbnailUrl = post.thumbnailUrl,
        thumbnailImage = post.thumbnailImage,
        thumbnailStatus = post.thumbnailStatus,
        description = post.description,
        teamId = post.teamId,
        tagTitles = post.tagTitles,
    )
}

data class PostBatchResult(
    val postCount: Int,
    val viewCount: Long,
)

data class ImageAssetData(
    val url: String,
    val width: Int?,
    val height: Int?,
    val aspectRatio: Double?,
) {
    constructor(imageAsset: ImageAsset) : this(
        url = imageAsset.url,
        width = imageAsset.width,
        height = imageAsset.height,
        aspectRatio = imageAsset.aspectRatio,
    )
}

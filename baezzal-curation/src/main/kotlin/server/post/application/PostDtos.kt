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

data class CreatePostResult(
    val postId: Long,
    val memberId: Long,
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
    ) : this(
        postId = post.id,
        memberId = post.memberId,
        imageUrl = post.originalImage.url,
        originalImage = ImageAssetData(post.originalImage),
        thumbnailUrl = post.thumbnailImage.url,
        thumbnailImage = ImageAssetData(post.thumbnailImage),
        thumbnailStatus = post.thumbnailStatus.name,
        description = post.description,
        teamId = post.teamId,
        tagTitles = tags.map(Tag::title),
    )
}

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

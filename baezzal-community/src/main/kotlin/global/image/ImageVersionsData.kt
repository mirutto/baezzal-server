package global.image

data class ImageVersionsData(
    val rawUrl: String,
    val publicUrl: String,
    val thumbnailUrl: String,
    val status: String,
    val aspectRatio: Double,
) {
    constructor(image: ImageVersions) : this(
        rawUrl = image.rawUrl,
        publicUrl = image.publicUrl,
        thumbnailUrl = image.thumbnailUrl,
        status = image.status.name,
        aspectRatio = image.aspectRatio,
    )
}

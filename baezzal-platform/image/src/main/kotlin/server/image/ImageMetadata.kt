package server.image

data class ImageMetadata(
    val width: Int,
    val height: Int,
    val mimeType: String,
    val fileExtension: String,
    val orientation: Int?,
)

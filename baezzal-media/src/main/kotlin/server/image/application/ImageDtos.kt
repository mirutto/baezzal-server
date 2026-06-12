package server.image.application

import server.image.ImageMetadata

data class ImageData(
    val bytes: ByteArray,
    val width: Int,
    val height: Int,
    val mimeType: String,
    val fileExtension: String,
    val aspectRatio: Double,
    val orientation: Int?
) {

    constructor(
        imageBytes: ByteArray,
        imageMetadata: ImageMetadata,
    ) : this(
        bytes = imageBytes,
        width = imageMetadata.width,
        height = imageMetadata.height,
        mimeType = imageMetadata.mimeType,
        fileExtension = imageMetadata.fileExtension,
        aspectRatio = imageMetadata.width.toDouble() / imageMetadata.height.toDouble(),
        orientation = imageMetadata.orientation,
    )

    fun toMetadata(): ImageMetadata {
        return ImageMetadata(
            width = width,
            height = height,
            aspectRatio = aspectRatio,
            mimeType = mimeType,
            fileExtension = fileExtension,
            orientation = orientation,
        )
    }
}

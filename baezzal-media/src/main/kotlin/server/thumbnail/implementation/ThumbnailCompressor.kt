package server.thumbnail.implementation

import org.springframework.stereotype.Component
import server.image.CompressedImage
import server.image.ImageMetadata
import server.image.WebpThumbnailCompressor

@Component
class ThumbnailCompressor(
    private val webpThumbnailCompressor: WebpThumbnailCompressor,
) {
    fun compress(
        imageBytes: ByteArray,
        metadata: ImageMetadata,
    ): CompressedImage = webpThumbnailCompressor.compress(imageBytes, metadata)
}

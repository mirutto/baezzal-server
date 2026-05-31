package server.thumbnail.implementation

import org.springframework.stereotype.Component
import server.image.ImageMetadata
import server.image.ImageMetadataExtractor

@Component
class ThumbnailMetadataExtractor(
    private val imageMetadataExtractor: ImageMetadataExtractor,
) {
    fun extract(imageBytes: ByteArray): ImageMetadata = imageMetadataExtractor.extract(imageBytes)
}

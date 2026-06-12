package server.image.implementation

import org.springframework.stereotype.Component
import server.image.ImageMetadataExtractor
import server.image.WebpImageCompressor
import server.image.application.ImageData

@Component
class ImageCompressor(
    private val webpImageCompressor: WebpImageCompressor,
    private val imageMetadataExtractor: ImageMetadataExtractor
) {

    fun compress(
        image: ImageData,
        minWidth: Int,
    ): ImageData {
        val compressed = webpImageCompressor.compress(
            image.bytes,
            image.toMetadata(),
            minWidth
        )
        val compressedMetadata = imageMetadataExtractor.extract(compressed.bytes)

        return ImageData(compressed.bytes, compressedMetadata)
    }
}

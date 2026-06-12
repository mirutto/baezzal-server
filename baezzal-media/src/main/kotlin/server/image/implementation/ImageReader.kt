package server.image.implementation

import org.springframework.stereotype.Component
import server.image.ImageMetadataExtractor
import server.image.ImageUrlReader
import server.image.application.ImageData

@Component
class ImageReader(
    private val imageUrlReader: ImageUrlReader,
    private val imageMetadataExtractor: ImageMetadataExtractor,
) {

    fun read(imageUrl: String): ImageData {
        val byte = imageUrlReader.readBytes(imageUrl)
        val metadata = imageMetadataExtractor.extract(byte)

        return ImageData(byte, metadata)
    }
}

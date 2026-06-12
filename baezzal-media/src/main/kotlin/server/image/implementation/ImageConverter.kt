package server.image.implementation

import org.springframework.stereotype.Component
import server.image.ImageMetadataExtractor
import server.image.PngImageConverter
import server.image.application.ImageData

@Component
class ImageConverter(
    private val pngImageConverter: PngImageConverter,
    private val imageMetadataExtractor: ImageMetadataExtractor
) {

    fun convert(image: ImageData): ImageData {
        val converted = pngImageConverter.convert(
            bytes = image.bytes,
            metadata = image.toMetadata()
        )
        val convertedMetadata = imageMetadataExtractor.extract(converted.bytes)

        return ImageData(converted.bytes, convertedMetadata)
    }
}

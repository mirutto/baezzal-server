package server.thumbnail.applicaiton

import org.springframework.stereotype.Service
import server.image.ImageUrlReader
import server.thumbnail.implementation.ThumbnailCompressor
import server.thumbnail.implementation.ThumbnailMetadataExtractor
import server.thumbnail.implementation.ThumbnailUploader
import java.util.UUID

@Service
class ThumbnailService(
    private val imageUrlReader: ImageUrlReader,
    private val thumbnailMetadataExtractor: ThumbnailMetadataExtractor,
    private val thumbnailCompressor: ThumbnailCompressor,
    private val thumbnailUploader: ThumbnailUploader,
) {
    fun createThumbnail(imageUrl: String): String {
        val imageBytes = imageUrlReader.readBytes(imageUrl)
        val metadata = thumbnailMetadataExtractor.extract(imageBytes)
        val thumbnailImage = thumbnailCompressor.compress(imageBytes, metadata)

        return thumbnailUploader.upload(
            fileName = "${UUID.randomUUID()}.${thumbnailImage.fileExtension}",
            image = thumbnailImage,
        )
    }
}

package server.thumbnail.applicaiton

import org.springframework.stereotype.Service
import server.image.ImageUrlReader
import server.thumbnail.implementation.ThumbnailCompressor
import server.thumbnail.implementation.ThumbnailEventPublisher
import server.thumbnail.implementation.ThumbnailMetadataExtractor
import server.thumbnail.implementation.ThumbnailUploader
import java.util.UUID

@Service
class ThumbnailService(
    private val imageUrlReader: ImageUrlReader,
    private val thumbnailMetadataExtractor: ThumbnailMetadataExtractor,
    private val thumbnailCompressor: ThumbnailCompressor,
    private val thumbnailUploader: ThumbnailUploader,
    private val thumbnailEventPublisher: ThumbnailEventPublisher,
) {
    fun createThumbnail(
        portId: Long,
        imageUrl: String,
    ) {
        val imageBytes = imageUrlReader.readBytes(imageUrl)
        val metadata = thumbnailMetadataExtractor.extract(imageBytes)
        val thumbnailImage = thumbnailCompressor.compress(imageBytes, metadata)
        val thumbnailMetadata = thumbnailMetadataExtractor.extract(thumbnailImage.bytes)

        val thumbnailUrl = thumbnailUploader.upload(
            fileName = "${UUID.randomUUID()}.${thumbnailImage.fileExtension}",
            image = thumbnailImage,
        )

        thumbnailEventPublisher.publishUploaded(
            postId = portId,
            originalImage = ImageAssetEvent(
                url = imageUrl,
                width = metadata.width,
                height = metadata.height,
                aspectRatio = metadata.toAspectRatio(),
            ),
            thumbnailImage = ImageAssetEvent(
                url = thumbnailUrl,
                width = thumbnailMetadata.width,
                height = thumbnailMetadata.height,
                aspectRatio = thumbnailMetadata.toAspectRatio(),
            ),
        )
    }
}

private fun server.image.ImageMetadata.toAspectRatio(): Double =
    width.toDouble() / height.toDouble()

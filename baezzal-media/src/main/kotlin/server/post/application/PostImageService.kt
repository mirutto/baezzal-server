package server.post.application

import org.springframework.stereotype.Component
import server.image.implementation.ImageReader
import server.post.implementation.PostImageEventPublisher
import server.post.implementation.PublicImageConverter
import server.post.implementation.PublicImageUploader
import server.post.implementation.ThumbnailImageCompressor
import server.post.implementation.ThumbnailImageUploader

@Component
class PostImageService(
    private val imageReader: ImageReader,
    private val publicImageConverter: PublicImageConverter,
    private val publicImageUploader: PublicImageUploader,
    private val thumbnailImageCompressor: ThumbnailImageCompressor,
    private val thumbnailImageUploader: ThumbnailImageUploader,
    private val postImageEventPublisher: PostImageEventPublisher
) {

    fun proceed(
        portId: Long,
        imageUrl: String,
    ) {
        val rawImage = imageReader.read(imageUrl)

        val publicImage = publicImageConverter.convert(rawImage)
        val publicImageUrl = publicImageUploader.upload(publicImage)

        val thumbnailImage = thumbnailImageCompressor.compress(rawImage)
        val thumbnailImageUrl = thumbnailImageUploader.upload(thumbnailImage)

        postImageEventPublisher.publishProcessed(
            postId = portId,
            rawImageUrl = imageUrl,
            publicImageUrl = publicImageUrl,
            thumbnailImageUrl = thumbnailImageUrl,
            aspectRatio = publicImage.aspectRatio,
        )
    }
}

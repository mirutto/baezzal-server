package server.post.implementation

import org.springframework.stereotype.Component
import server.image.application.ImageData
import server.image.implementation.ImageCompressor

@Component
class ThumbnailImageCompressor(
    private val imageCompressor: ImageCompressor,
) {

    fun compress(imageData: ImageData): ImageData =
        imageCompressor.compress(imageData, POST_THUMBNAIL_MAX_WIDTH)

    companion object {
        private const val POST_THUMBNAIL_MAX_WIDTH = 200
    }
}

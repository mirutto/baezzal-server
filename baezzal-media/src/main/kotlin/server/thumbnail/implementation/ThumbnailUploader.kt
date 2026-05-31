package server.thumbnail.implementation

import org.springframework.stereotype.Component
import server.image.CompressedImage
import server.objectstorage.ObjectStorage
import java.io.ByteArrayInputStream

@Component
class ThumbnailUploader(
    private val objectStorage: ObjectStorage,
) {
    fun upload(
        fileName: String,
        image: CompressedImage,
    ): String =
        ByteArrayInputStream(image.bytes).use { inputStream ->
            objectStorage.uploadImage(
                prefix = THUMBNAIL_PREFIX,
                fileName = fileName,
                contentType = image.contentType,
                inputStream = inputStream,
                size = image.bytes.size.toLong(),
            )
        }

    companion object {
        private const val THUMBNAIL_PREFIX = "thumbnails"
    }
}

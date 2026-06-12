package server.post.implementation

import org.springframework.stereotype.Component
import server.image.application.ImageData
import server.objectstorage.ObjectStorage
import java.io.ByteArrayInputStream
import java.util.UUID

@Component
class PublicImageUploader(
    private val objectStorage: ObjectStorage,
) {
    fun upload(publicImage: ImageData): String =
        ByteArrayInputStream(publicImage.bytes).use { inputStream ->
            objectStorage.uploadImage(
                prefix = POST_PUBLIC_PREFIX,
                fileName = "${UUID.randomUUID()}.${publicImage.fileExtension}",
                contentType = publicImage.mimeType,
                inputStream = inputStream,
                size = publicImage.bytes.size.toLong(),
            )
        }

    companion object {
        private const val POST_PUBLIC_PREFIX = "posts/public"
    }
}

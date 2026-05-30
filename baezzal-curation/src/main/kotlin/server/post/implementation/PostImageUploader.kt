package server.post.implementation

import org.springframework.stereotype.Component
import server.objectstorage.ObjectStorage
import server.objectstorage.PresignedUploadUrl

@Component
class PostImageUploader(
    private val objectStorage: ObjectStorage,
) {
    fun createPresignedUploadUrl(
        prefix: String,
        fileName: String,
        contentType: String,
    ): PresignedUploadUrl =
        objectStorage.createPresignedImageUploadUrl(
            prefix = prefix,
            fileName = fileName,
            contentType = contentType,
        )
}

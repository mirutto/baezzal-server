package server.image.upload

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

@Service
class PresignedImageUploadUrlService(
    private val minioImageStorage: MinioImageStorage,
    @param:Value("\${upload.image.bucket:baezzal-images}")
    private val bucket: String,
    @param:Value("\${upload.image.prefix:images}")
    private val prefix: String,
    @param:Value("\${upload.image.presigned-expiry-seconds:600}")
    private val presignedExpirySeconds: Int,
) {
    fun issue(
        fileName: String,
        contentType: String,
    ): PresignedImageUploadUrl {
        minioImageStorage.ensureBucketExists(bucket)

        val objectKey = createObjectKey(fileName)
        val uploadUrl = minioImageStorage.createPresignedPutUrl(
            bucket = bucket,
            objectKey = objectKey,
            expirySeconds = presignedExpirySeconds,
        )
        val fileUrl = minioImageStorage.createFileUrl(
            bucket = bucket,
            objectKey = objectKey,
        )

        return PresignedImageUploadUrl(
            objectKey = objectKey,
            uploadUrl = uploadUrl,
            fileUrl = fileUrl,
            headers = mapOf(CONTENT_TYPE_HEADER to contentType),
            expiresInSeconds = presignedExpirySeconds,
        )
    }

    private fun createObjectKey(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val datePath = LocalDate.now().toString()
        val normalizedPrefix = prefix.trim('/').ifBlank { DEFAULT_PREFIX }
        val uuid = UUID.randomUUID().toString()

        return buildString {
            append(normalizedPrefix)
            append('/')
            append(datePath)
            append('/')
            append(uuid)
            if (extension.isNotBlank()) {
                append('.')
                append(extension)
            }
        }
    }

    companion object {
        private const val CONTENT_TYPE_HEADER = "Content-Type"
        private const val DEFAULT_PREFIX = "images"
    }
}

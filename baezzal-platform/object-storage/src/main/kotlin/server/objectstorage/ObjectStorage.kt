package server.objectstorage

import io.minio.BucketExistsArgs
import io.minio.GetPresignedObjectUrlArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.http.Method
import org.springframework.stereotype.Component
import server.config.MinioProperties
import java.io.InputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.TimeUnit

@Component
class ObjectStorage(
    private val minioClient: MinioClient,
    private val minioProperties: MinioProperties,
    private val imageUploadProperties: ImageUploadProperties,
) {
    fun createPresignedImageUploadUrl(
        fileName: String,
        contentType: String,
    ): PresignedUploadUrl {
        ensureBucketExists(imageUploadProperties.bucket)

        val objectKey = createObjectKey(fileName)
        val uploadUrl =
            minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs
                    .builder()
                    .method(Method.PUT)
                    .bucket(imageUploadProperties.bucket)
                    .`object`(objectKey)
                    .expiry(imageUploadProperties.presignedExpirySeconds, TimeUnit.SECONDS)
                    .build(),
            )

        return PresignedUploadUrl(
            objectKey = objectKey,
            uploadUrl = uploadUrl,
            fileUrl = createFileUrl(imageUploadProperties.bucket, objectKey),
            headers = mapOf(CONTENT_TYPE_HEADER to contentType),
            expiresInSeconds = imageUploadProperties.presignedExpirySeconds,
        )
    }

    fun uploadImage(
        fileName: String,
        contentType: String,
        inputStream: InputStream,
        size: Long,
    ): String {
        ensureBucketExists(imageUploadProperties.bucket)

        val objectKey = createObjectKey(fileName)
        minioClient.putObject(
            PutObjectArgs
                .builder()
                .bucket(imageUploadProperties.bucket)
                .`object`(objectKey)
                .stream(inputStream, size, -1)
                .contentType(contentType)
                .build(),
        )

        return createFileUrl(imageUploadProperties.bucket, objectKey)
    }

    private fun ensureBucketExists(bucket: String) {
        val bucketExists =
            minioClient.bucketExists(
                BucketExistsArgs
                    .builder()
                    .bucket(bucket)
                    .build(),
            )
        if (bucketExists) {
            return
        }

        minioClient.makeBucket(
            MakeBucketArgs
                .builder()
                .bucket(bucket)
                .build(),
        )
    }

    private fun createObjectKey(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val datePath = LocalDate.now().toString()
        val normalizedPrefix = imageUploadProperties.prefix.trim('/').ifBlank { DEFAULT_PREFIX }
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

    private fun createFileUrl(
        bucket: String,
        objectKey: String,
    ): String =
        buildString {
            append(minioProperties.publicEndpoint.trimEnd('/'))
            append('/')
            append(encodePathSegment(bucket))

            objectKey.split('/').filter { it.isNotBlank() }.forEach {
                append('/')
                append(encodePathSegment(it))
            }
        }

    private fun encodePathSegment(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8)
            .replace("+", "%20")

    companion object {
        private const val CONTENT_TYPE_HEADER = "Content-Type"
        private const val DEFAULT_PREFIX = "images"
    }
}

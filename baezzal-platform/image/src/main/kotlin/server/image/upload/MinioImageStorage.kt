package server.image.upload

import io.minio.BucketExistsArgs
import io.minio.GetPresignedObjectUrlArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.http.Method
import org.springframework.stereotype.Component
import server.config.MinioProperties
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

@Component
class MinioImageStorage(
    private val minioClient: MinioClient,
    private val minioProperties: MinioProperties,
) {
    fun ensureBucketExists(bucket: String) {
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

    fun createPresignedPutUrl(
        bucket: String,
        objectKey: String,
        expirySeconds: Int,
    ): String =
        minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs
                .builder()
                .method(Method.PUT)
                .bucket(bucket)
                .`object`(objectKey)
                .expiry(expirySeconds, TimeUnit.SECONDS)
                .build(),
        )

    fun createFileUrl(
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
}

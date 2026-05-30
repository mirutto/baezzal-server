package server.objectstorage

import io.kotest.matchers.shouldBe
import io.minio.GetPresignedObjectUrlArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.config.MinioProperties

class ObjectStorageTest {
    private val minioClient = mockk<MinioClient>()
    private val imageUploadProperties =
        ImageUploadProperties().apply {
            bucket = "baezzal-images"
            presignedExpirySeconds = 600
        }
    private val objectStorage = ObjectStorage(
        minioClient = minioClient,
        minioProperties = MinioProperties(
            uploadEndpoint = "https://s3.wowan.me",
            publicEndpoint = "https://static.wowan.me",
        ),
        imageUploadProperties = imageUploadProperties,
    )

    @Test
    fun `object key 를 생성하고 presigned url 을 반환한다`() {
        val presignedArgs = slot<GetPresignedObjectUrlArgs>()
        every { minioClient.bucketExists(any()) } returns true
        every { minioClient.getPresignedObjectUrl(capture(presignedArgs)) } returns "https://s3.wowan.me/upload"

        val result = objectStorage.createPresignedImageUploadUrl(
            prefix = "posts",
            fileName = "sample.PNG",
            contentType = "image/png",
        )

        result.uploadUrl shouldBe "https://s3.wowan.me/upload"
        result.fileUrl shouldBe "https://static.wowan.me/baezzal-images/${result.objectKey}"
        result.headers shouldBe mapOf("Content-Type" to "image/png")
        result.expiresInSeconds shouldBe 600
        result.objectKey shouldBe "posts/sample.PNG"
        presignedArgs.captured.`object`() shouldBe result.objectKey
        verify(exactly = 1) { minioClient.bucketExists(any()) }
    }

    @Test
    fun `직접 업로드 후 공개 url 을 반환한다`() {
        val putArgs = slot<PutObjectArgs>()
        every { minioClient.bucketExists(any()) } returns true
        every { minioClient.putObject(capture(putArgs)) } returns mockk()

        val result =
            objectStorage.uploadImage(
                prefix = "profiles",
                fileName = "photo.jpg",
                contentType = "image/jpeg",
                inputStream = "test".byteInputStream(),
                size = 4,
            )

        result shouldBe "https://static.wowan.me/baezzal-images/profiles/photo.jpg"
        putArgs.captured.contentType() shouldBe "image/jpeg"
    }
}

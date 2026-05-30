package server.image.upload

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldStartWith
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test

class PresignedImageUploadUrlServiceTest {
    private val minioImageStorage = mockk<MinioImageStorage>()
    private val presignedImageUploadUrlService = PresignedImageUploadUrlService(
        minioImageStorage = minioImageStorage,
        bucket = "baezzal-images",
        prefix = "images",
        presignedExpirySeconds = 600,
    )

    @Test
    fun `object key 를 생성하고 presigned url 을 반환한다`() {
        val objectKey = slot<String>()
        every { minioImageStorage.ensureBucketExists("baezzal-images") } returns Unit
        every {
            minioImageStorage.createPresignedPutUrl(
                bucket = "baezzal-images",
                objectKey = capture(objectKey),
                expirySeconds = 600,
            )
        } returns "https://s3.wowan.me/upload"
        every {
            minioImageStorage.createFileUrl(
                bucket = "baezzal-images",
                objectKey = any(),
            )
        } answers {
            "https://s3.wowan.me/${secondArg<String>()}"
        }

        val result = presignedImageUploadUrlService.issue(
            fileName = "sample.PNG",
            contentType = "image/png",
        )

        result.uploadUrl shouldBe "https://s3.wowan.me/upload"
        result.fileUrl shouldBe "https://s3.wowan.me/${result.objectKey}"
        result.headers shouldBe mapOf("Content-Type" to "image/png")
        result.expiresInSeconds shouldBe 600
        result.objectKey shouldStartWith "images/"
        result.objectKey shouldEndWith ".png"
        objectKey.captured shouldBe result.objectKey
        verify(exactly = 1) { minioImageStorage.ensureBucketExists("baezzal-images") }
    }
}

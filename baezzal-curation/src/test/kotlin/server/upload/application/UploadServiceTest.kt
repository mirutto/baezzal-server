package server.upload.application

import global.error.BadRequestException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.image.upload.PresignedImageUploadUrl
import server.image.upload.PresignedImageUploadUrlService

class UploadServiceTest {
    private val presignedImageUploadUrlService = mockk<PresignedImageUploadUrlService>()
    private val uploadService = UploadService(presignedImageUploadUrlService)

    @Test
    fun `이미지 presigned url 을 발급한다`() {
        val issued = PresignedImageUploadUrl(
            objectKey = "images/2026-05-30/test.png",
            uploadUrl = "https://s3.wowan.me/put",
            fileUrl = "https://s3.wowan.me/file",
            headers = mapOf("Content-Type" to "image/png"),
            expiresInSeconds = 600,
        )
        every {
            presignedImageUploadUrlService.issue(
                fileName = "profile.png",
                contentType = "image/png",
            )
        } returns issued

        val actual = uploadService.createImageUploadUrl(
            CreateImageUploadUrlCommand(
                fileName = " profile.png ",
                contentType = " IMAGE/PNG ",
            ),
        )

        actual shouldBe ImageUploadUrlResult.from(issued)
        verify(exactly = 1) {
            presignedImageUploadUrlService.issue(
                fileName = "profile.png",
                contentType = "image/png",
            )
        }
    }

    @Test
    fun `빈 파일 이름이면 예외가 발생한다`() {
        shouldThrow<BadRequestException> {
            uploadService.createImageUploadUrl(
                CreateImageUploadUrlCommand(
                    fileName = "   ",
                    contentType = "image/png",
                ),
            )
        }
    }

    @Test
    fun `이미지가 아니면 예외가 발생한다`() {
        shouldThrow<BadRequestException> {
            uploadService.createImageUploadUrl(
                CreateImageUploadUrlCommand(
                    fileName = "document.pdf",
                    contentType = "application/pdf",
                ),
            )
        }
    }
}

package server.upload.application

import global.error.BadRequestException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.objectstorage.PresignedUploadUrl
import server.upload.implementation.UploadImageUploader

class UploadServiceTest {
    private val uploadImageUploader = mockk<UploadImageUploader>()
    private val uploadService = UploadService(uploadImageUploader)

    @Test
    fun `이미지 presigned url 을 발급한다`() {
        val issued = PresignedUploadUrl(
            objectKey = "profiles/profile.png",
            uploadUrl = "https://s3.wowan.me/put",
            fileUrl = "https://static.wowan.me/file",
            headers = mapOf("Content-Type" to "image/png"),
            expiresInSeconds = 600,
        )
        every {
            uploadImageUploader.createPresignedUploadUrl(
                prefix = "profiles",
                fileName = "profile.png",
                contentType = "image/png",
            )
        } returns issued

        val actual = uploadService.createImageUploadUrl(
            CreateImageUploadUrlCommand(
                prefix = " profiles ",
                fileName = " profile.png ",
                contentType = " IMAGE/PNG ",
            ),
        )

        actual shouldBe ImageUploadUrlResult.from(issued)
        verify(exactly = 1) {
            uploadImageUploader.createPresignedUploadUrl(
                prefix = "profiles",
                fileName = "profile.png",
                contentType = "image/png",
            )
        }
    }

    @Test
    fun `빈 prefix 이면 예외가 발생한다`() {
        shouldThrow<BadRequestException> {
            uploadService.createImageUploadUrl(
                CreateImageUploadUrlCommand(
                    prefix = "   ",
                    fileName = "profile.png",
                    contentType = "image/png",
                ),
            )
        }
    }

    @Test
    fun `빈 파일 이름이면 예외가 발생한다`() {
        shouldThrow<BadRequestException> {
            uploadService.createImageUploadUrl(
                CreateImageUploadUrlCommand(
                    prefix = "profiles",
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
                    prefix = "profiles",
                    fileName = "document.pdf",
                    contentType = "application/pdf",
                ),
            )
        }
    }
}

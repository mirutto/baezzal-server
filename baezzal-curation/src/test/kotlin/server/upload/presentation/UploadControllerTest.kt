package server.upload.presentation

import global.error.BadRequestException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.objectstorage.ObjectStorage
import server.objectstorage.PresignedUploadUrl
import server.upload.application.CreateImageUploadUrlCommand
import server.upload.application.ImageUploadUrlResult

class UploadControllerTest {
    private val objectStorage = mockk<ObjectStorage>()
    private val uploadController = UploadController(objectStorage)

    @Test
    fun `이미지 presigned url 을 발급한다`() {
        val issued = PresignedUploadUrl(
            objectKey = "images/2026-05-30/test.png",
            uploadUrl = "https://s3.wowan.me/put",
            fileUrl = "https://s3.wowan.me/file",
            headers = mapOf("Content-Type" to "image/png"),
            expiresInSeconds = 600,
        )
        every {
            objectStorage.createPresignedImageUploadUrl(
                fileName = "profile.png",
                contentType = "image/png",
            )
        } returns issued

        val actual =
            uploadController.createImageUploadUrl(
                CreateImageUploadUrlCommand(
                    fileName = " profile.png ",
                    contentType = " IMAGE/PNG ",
                ),
            )

        actual.body shouldBe ImageUploadUrlResult.from(issued)
        verify(exactly = 1) {
            objectStorage.createPresignedImageUploadUrl(
                fileName = "profile.png",
                contentType = "image/png",
            )
        }
    }

    @Test
    fun `빈 파일 이름이면 예외가 발생한다`() {
        shouldThrow<BadRequestException> {
            uploadController.createImageUploadUrl(
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
            uploadController.createImageUploadUrl(
                CreateImageUploadUrlCommand(
                    fileName = "document.pdf",
                    contentType = "application/pdf",
                ),
            )
        }
    }
}

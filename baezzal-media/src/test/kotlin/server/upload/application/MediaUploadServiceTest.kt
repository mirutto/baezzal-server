package server.upload.application

import global.error.BadRequestException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.objectstorage.PresignedUploadUrl
import server.upload.implementation.MediaUploadUrlRecorder
import server.upload.implementation.MediaUploadUrlIssuer

class MediaUploadServiceTest {
    private val mediaUploadUrlIssuer = mockk<MediaUploadUrlIssuer>()
    private val mediaUploadUrlRecorder = mockk<MediaUploadUrlRecorder>()
    private val mediaUploadService = MediaUploadService(
        mediaUploadUrlIssuer = mediaUploadUrlIssuer,
        mediaUploadUrlRecorder = mediaUploadUrlRecorder,
    )

    @Test
    fun `이미지 업로드 url 을 발급하고 검증용 캐시에 기록한다`() {
        val fileName = slot<String>()
        val issued = PresignedUploadUrl(
            objectKey = "posts/123e4567-e89b-12d3-a456-426614174000.png",
            uploadUrl = "https://s3.wowan.me/put",
            fileUrl = "https://static.wowan.me/file",
            headers = mapOf("Content-Type" to "image/png"),
            expiresInSeconds = 600,
        )
        every {
            mediaUploadUrlIssuer.createPresignedUploadUrl(
                prefix = "posts",
                fileName = capture(fileName),
                contentType = "image/png",
            )
        } returns issued
        every {
            mediaUploadUrlRecorder.recordIssued(
                prefix = "posts",
                fileUrl = issued.fileUrl,
                expiresInSeconds = issued.expiresInSeconds,
            )
        } returns Unit

        val result = mediaUploadService.createUploadUrl(
            memberId = 7L,
            command = CreateMediaUploadUrlCommand(
                prefix = "/posts/",
                contentType = " IMAGE/PNG ",
            ),
        )

        result shouldBe MediaUploadUrlResult.from(issued)
        FILE_NAME_REGEX.matches(fileName.captured) shouldBe true
        verify(exactly = 1) {
            mediaUploadUrlRecorder.recordIssued(
                prefix = "posts",
                fileUrl = issued.fileUrl,
                expiresInSeconds = issued.expiresInSeconds,
            )
        }
    }

    @Test
    fun `jpeg content type 은 jpg 확장자로 발급한다`() {
        val fileName = slot<String>()
        every {
            mediaUploadUrlIssuer.createPresignedUploadUrl(
                prefix = "posts",
                fileName = capture(fileName),
                contentType = "image/jpeg",
            )
        } returns
            PresignedUploadUrl(
                objectKey = "posts/123e4567-e89b-12d3-a456-426614174000.jpg",
                uploadUrl = "https://s3.wowan.me/put",
                fileUrl = "https://static.wowan.me/file",
                headers = mapOf("Content-Type" to "image/jpeg"),
                expiresInSeconds = 600,
            )
        every {
            mediaUploadUrlRecorder.recordIssued(
                prefix = "posts",
                fileUrl = any(),
                expiresInSeconds = 600,
            )
        } returns Unit

        mediaUploadService.createUploadUrl(
            memberId = 7L,
            command = CreateMediaUploadUrlCommand(
                prefix = "posts",
                contentType = "image/jpeg",
            ),
        )

        JPEG_FILE_NAME_REGEX.matches(fileName.captured) shouldBe true
    }

    @Test
    fun `prefix 가 비어 있으면 예외가 발생한다`() {
        shouldThrow<BadRequestException> {
            mediaUploadService.createUploadUrl(
                memberId = 7L,
                command = CreateMediaUploadUrlCommand(
                    prefix = " / ",
                    contentType = "image/png",
                ),
            )
        }

        verify(exactly = 0) {
            mediaUploadUrlIssuer.createPresignedUploadUrl(any(), any(), any())
        }
    }

    @Test
    fun `이미지가 아니면 예외가 발생한다`() {
        shouldThrow<BadRequestException> {
            mediaUploadService.createUploadUrl(
                memberId = 7L,
                command = CreateMediaUploadUrlCommand(
                    prefix = "posts",
                    contentType = "application/pdf",
                ),
            )
        }

        verify(exactly = 0) {
            mediaUploadUrlIssuer.createPresignedUploadUrl(any(), any(), any())
        }
    }

    companion object {
        private val FILE_NAME_REGEX =
            Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.png$")
        private val JPEG_FILE_NAME_REGEX =
            Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.jpg$")
    }
}

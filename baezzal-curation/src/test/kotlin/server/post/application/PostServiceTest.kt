package server.post.application

import global.error.BadRequestException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.objectstorage.PresignedUploadUrl
import server.post.implementation.PostImageUploader

class PostServiceTest {
    private val postImageUploader = mockk<PostImageUploader>()
    private val postService = PostService(postImageUploader)

    @Test
    fun `post 이미지 presigned url 을 발급한다`() {
        val fileName = slot<String>()
        val issued = PresignedUploadUrl(
            objectKey = "posts/123e4567-e89b-12d3-a456-426614174000",
            uploadUrl = "https://s3.wowan.me/put",
            fileUrl = "https://static.wowan.me/file",
            headers = mapOf("Content-Type" to "image/png"),
            expiresInSeconds = 600,
        )
        every {
            postImageUploader.createPresignedUploadUrl(
                prefix = "posts",
                fileName = capture(fileName),
                contentType = "image/png",
            )
        } returns issued

        val actual = postService.createImageUploadUrl(
            CreatePostImageUploadUrlCommand(
                contentType = " IMAGE/PNG ",
            ),
        )

        actual shouldBe PostImageUploadUrlResult.from(issued)
        UUID_REGEX.matches(fileName.captured) shouldBe true
        verify(exactly = 1) {
            postImageUploader.createPresignedUploadUrl(
                prefix = "posts",
                fileName = any(),
                contentType = "image/png",
            )
        }
    }

    @Test
    fun `이미지가 아니면 예외가 발생한다`() {
        shouldThrow<BadRequestException> {
            postService.createImageUploadUrl(
                CreatePostImageUploadUrlCommand(
                    contentType = "application/pdf",
                ),
            )
        }
    }

    companion object {
        private val UUID_REGEX =
            Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
    }
}

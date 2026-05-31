package server.post.application

import global.error.BadRequestException
import global.error.NotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.objectstorage.PresignedUploadUrl
import server.post.domain.Post
import server.post.implementation.PostEventPublisher
import server.post.implementation.PostImageUploader
import server.post.implementation.PostImageUrlRecorder
import server.post.implementation.PostValidator
import server.post.implementation.PostWriter
import server.posttag.implementation.PostTagWriter
import server.tag.domain.Tag
import server.tag.implementation.TagResolver

class PostServiceTest {
    private val postImageUploader = mockk<PostImageUploader>()
    private val postImageUrlRecorder = mockk<PostImageUrlRecorder>()
    private val postValidator = mockk<PostValidator>()
    private val postWriter = mockk<PostWriter>()
    private val postTagWriter = mockk<PostTagWriter>()
    private val tagResolver = mockk<TagResolver>()
    private val postEventPublisher = mockk<PostEventPublisher>()
    private val postService = PostService(
        postImageUploader = postImageUploader,
        postImageUrlRecorder = postImageUrlRecorder,
        postValidator = postValidator,
        postWriter = postWriter,
        postTagWriter = postTagWriter,
        tagResolver = tagResolver,
        postEventPublisher = postEventPublisher,
    )

    @Test
    fun `post 를 생성한다`() {
        val savedPost = slot<Post>()
        val resolvedTags = listOf(
            Tag(id = 10L, title = "KBO"),
            Tag(id = 11L, title = "잠실"),
        )

        every { postValidator.validateImageUrl("https://cdn.example.com/post.png") } returns Unit
        every { postValidator.normalizeTeamId(2L) } returns 2L
        every { postWriter.write(capture(savedPost)) } returns Post(
            id = 100L,
            memberId = 7L,
            imageUrl = "https://cdn.example.com/post.png",
            description = "경기 후기",
            teamId = 2L,
        )
        every { tagResolver.resolveAll(listOf("KBO", "잠실", "KBO")) } returns resolvedTags
        every { postTagWriter.writeAll(100L, resolvedTags) } returns emptyList()
        every { postEventPublisher.publishCreated(any()) } returns Unit

        val result = postService.create(
            memberId = 7L,
            command = CreatePostCommand(
                imageUrl = " https://cdn.example.com/post.png ",
                description = " 경기 후기 ",
                teamId = 2L,
                tagTitles = listOf("KBO", "잠실", "KBO"),
            ),
        )

        result shouldBe CreatePostResult(
            postId = 100L,
            memberId = 7L,
            imageUrl = "https://cdn.example.com/post.png",
            thumbnailUrl = "",
            thumbnailStatus = "PENDING",
            description = "경기 후기",
            teamId = 2L,
            tagTitles = listOf("KBO", "잠실"),
        )
        savedPost.captured.memberId shouldBe 7L
        savedPost.captured.teamId shouldBe 2L
        savedPost.captured.description shouldBe "경기 후기"
        verify(exactly = 1) {
            postEventPublisher.publishCreated(
                Post(
                    id = 100L,
                    memberId = 7L,
                    imageUrl = "https://cdn.example.com/post.png",
                    description = "경기 후기",
                    teamId = 2L,
                ),
            )
        }
    }

    @Test
    fun `team id 가 0 이면 null 로 저장한다`() {
        val savedPost = slot<Post>()

        every { postValidator.validateImageUrl("https://cdn.example.com/post.png") } returns Unit
        every { postValidator.normalizeTeamId(0L) } returns null
        every { postWriter.write(capture(savedPost)) } returns Post(
            id = 101L,
            memberId = 9L,
            imageUrl = "https://cdn.example.com/post.png",
            teamId = null,
        )
        every { tagResolver.resolveAll(emptyList()) } returns emptyList()
        every { postTagWriter.writeAll(101L, emptyList()) } returns emptyList()
        every { postEventPublisher.publishCreated(any()) } returns Unit

        val result = postService.create(
            memberId = 9L,
            command = CreatePostCommand(
                imageUrl = "https://cdn.example.com/post.png",
                teamId = 0L,
            ),
        )

        result.teamId shouldBe null
        result.memberId shouldBe 9L
        savedPost.captured.memberId shouldBe 9L
        savedPost.captured.teamId shouldBe null
        verify(exactly = 1) {
            postEventPublisher.publishCreated(
                Post(
                    id = 101L,
                    memberId = 9L,
                    imageUrl = "https://cdn.example.com/post.png",
                    teamId = null,
                ),
            )
        }
    }

    @Test
    fun `존재하지 않는 team id 이면 예외가 발생한다`() {
        every { postValidator.validateImageUrl("https://cdn.example.com/post.png") } returns Unit
        every { postValidator.normalizeTeamId(99L) } throws NotFoundException("팀을 찾을 수 없습니다")

        shouldThrow<NotFoundException> {
            postService.create(
                memberId = 3L,
                command = CreatePostCommand(
                    imageUrl = "https://cdn.example.com/post.png",
                    teamId = 99L,
                ),
            )
        }
    }

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
        every { postValidator.validateImageContentType("image/png") } returns Unit
        every {
            postImageUploader.createPresignedUploadUrl(
                prefix = "posts",
                fileName = capture(fileName),
                contentType = "image/png",
            )
        } returns issued
        every { postImageUrlRecorder.record("https://static.wowan.me/file", 600) } returns Unit

        val actual = postService.createImageUploadUrl(
            memberId = 7L,
            command = CreatePostImageUploadUrlCommand(
                contentType = " IMAGE/PNG ",
            ),
        )

        actual shouldBe PostImageUploadUrlResult.from(issued)
        UUID_REGEX.matches(fileName.captured) shouldBe true
        verify(exactly = 1) { postImageUrlRecorder.record("https://static.wowan.me/file", 600) }
    }

    @Test
    fun `이미지가 아니면 예외가 발생한다`() {
        every { postValidator.validateImageContentType("application/pdf") } throws
            BadRequestException("이미지 파일만 업로드할 수 있습니다")

        shouldThrow<BadRequestException> {
            postService.createImageUploadUrl(
                memberId = 7L,
                command = CreatePostImageUploadUrlCommand(
                    contentType = "application/pdf",
                ),
            )
        }
    }

    @Test
    fun `image url 이 비어 있으면 예외가 발생한다`() {
        every { postValidator.validateImageUrl("") } throws BadRequestException("imageUrl 은 비어 있을 수 없습니다")

        shouldThrow<BadRequestException> {
            postService.create(
                memberId = 5L,
                command = CreatePostCommand(
                    imageUrl = "   ",
                ),
            )
        }

        verify(exactly = 0) { postWriter.write(any()) }
        verify(exactly = 0) { tagResolver.resolveAll(any()) }
        verify(exactly = 0) { postTagWriter.writeAll(any<Long>(), any<List<Tag>>()) }
        verify(exactly = 0) { postEventPublisher.publishCreated(any()) }
    }

    companion object {
        private val UUID_REGEX =
            Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
    }
}

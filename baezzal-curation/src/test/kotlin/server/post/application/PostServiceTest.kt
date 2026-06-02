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
import server.post.application.MediaUploadUrlIssuedEvent
import server.post.domain.Post
import server.post.implementation.PostEventPublisher
import server.post.implementation.PostImageUrlRecorder
import server.post.implementation.PostReader
import server.post.implementation.PostValidator
import server.post.implementation.PostWriter
import server.posttag.implementation.PostTagWriter
import server.tag.domain.Tag
import server.tag.implementation.TagResolver

class PostServiceTest {
    private val postImageUrlRecorder = mockk<PostImageUrlRecorder>()
    private val postValidator = mockk<PostValidator>()
    private val postReader = mockk<PostReader>()
    private val postWriter = mockk<PostWriter>()
    private val postTagWriter = mockk<PostTagWriter>()
    private val tagResolver = mockk<TagResolver>()
    private val postEventPublisher = mockk<PostEventPublisher>()
    private val postService = PostService(
        postImageUrlRecorder = postImageUrlRecorder,
        postValidator = postValidator,
        postReader = postReader,
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
    fun `post prefix 로 발급된 image url 을 기록한다`() {
        every { postImageUrlRecorder.record("https://static.wowan.me/file", 600) } returns Unit

        postService.recordIssuedImageUrl(
            MediaUploadUrlIssuedEvent(
                prefix = "posts",
                objectKey = "posts/123e4567-e89b-12d3-a456-426614174000",
                fileUrl = "https://static.wowan.me/file",
                expiresInSeconds = 600,
            ),
        )

        verify(exactly = 1) { postImageUrlRecorder.record("https://static.wowan.me/file", 600) }
    }

    @Test
    fun `post prefix 가 아니면 image url 을 기록하지 않는다`() {
        postService.recordIssuedImageUrl(
            MediaUploadUrlIssuedEvent(
                prefix = "profiles",
                objectKey = "profiles/123e4567-e89b-12d3-a456-426614174000",
                fileUrl = "https://static.wowan.me/file",
                expiresInSeconds = 600,
            ),
        )

        verify(exactly = 0) { postImageUrlRecorder.record(any(), any()) }
    }

    @Test
    fun `thumbnail 을 업데이트한다`() {
        val post = Post(
            id = 100L,
            memberId = 7L,
            imageUrl = "https://cdn.example.com/post.png",
        )
        every { postReader.readById(100L) } returns post

        postService.updateThumbnail(
            postId = 100L,
            thumbnailUrl = " https://static.wowan.me/thumbnails/post.webp ",
        )

        post.thumbnailUrl shouldBe "https://static.wowan.me/thumbnails/post.webp"
        post.thumbnailStatus.name shouldBe "SUCCESS"
    }

    @Test
    fun `thumbnail 업데이트 대상 post 가 없으면 예외가 발생한다`() {
        every { postReader.readById(999L) } returns null

        shouldThrow<NotFoundException> {
            postService.updateThumbnail(
                postId = 999L,
                thumbnailUrl = "https://static.wowan.me/thumbnails/post.webp",
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
}
